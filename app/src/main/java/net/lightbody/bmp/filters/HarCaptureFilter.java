package net.lightbody.bmp.filters;

import android.util.Log;

import com.google.common.collect.ImmutableList;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarCookie;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarPostData;
import net.lightbody.bmp.core.har.HarPostDataParam;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;
import net.lightbody.bmp.exception.UnsupportedCharsetException;
import net.lightbody.bmp.filters.support.HttpConnectTiming;
import net.lightbody.bmp.filters.util.HarCaptureUtil;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.util.BrowserMobHttpUtil;

import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import me.moxun.dreamcatcher.wrapper.DCRequest;
import me.moxun.dreamcatcher.wrapper.DCResponse;
import me.moxun.dreamcatcher.wrapper.ProxyManager;

public class HarCaptureFilter extends HttpsAwareFiltersAdapter {
    private static final Logger log = LoggerFactory.getLogger(HarCaptureFilter.class);

    /**
     * The currently active HAR at the time the current request is received.
     */
    private final Har har;

    /**
     * The harEntry is created when this filter is constructed and is shared by both the clientToProxyRequest
     * and serverToProxyResponse methods. It is added to the HarLog when the request is received from the client.
     */
    @Deprecated
    private final HarEntry harEntry;

    private DCRequest harRequest;
    private DCResponse harResponse;
    private ProxyManager proxyManager;

    /**
     * The requestCaptureFilter captures all request content, including headers, trailing headers, and content. The HarCaptureFilter
     * delegates to it when the clientToProxyRequest() callback is invoked. If this request does not need content capture, the
     * ClientRequestCaptureFilter filter will not be instantiated and will not capture content.
     */
    private final ClientRequestCaptureFilter requestCaptureFilter;

    /**
     * Like requestCaptureFilter above, HarCaptureFilter delegates to responseCaptureFilter to capture response contents. If content capture
     * is not required for this request, the filter will not be instantiated or invoked.
     */
    private final ServerResponseCaptureFilter responseCaptureFilter;

    /**
     * The CaptureType data types to capture in this request.
     */
    private final EnumSet<CaptureType> dataToCapture;

    /**
     * Populated by proxyToServerResolutionStarted when DNS resolution starts. If any previous filters already resolved the address, their resolution time
     * will not be included in this time.
     */
    private volatile long dnsResolutionStartedNanos;

    private volatile long connectionQueuedNanos;
    private volatile long connectionStartedNanos;

    private volatile long sendStartedNanos;
    private volatile long sendFinishedNanos;

    private volatile long responseReceiveStartedNanos;

    /**
     * The address of the client making the request. Captured in the constructor and used when calculating and capturing ssl handshake and connect
     * timing information for SSL connections.
     */
    private final InetSocketAddress clientAddress;

    /**
     * Request body size is determined by the actual size of the data the client sends. The filter does not use the Content-Length header to determine request size.
     */
    private final AtomicInteger requestBodySize = new AtomicInteger(0);

    /**
     * Response body size is determined by the actual size of the data the server sends.
     */
    private final AtomicInteger responseBodySize = new AtomicInteger(0);

    /**
     * The "real" original request, as captured by the {@link #clientToProxyRequest(io.netty.handler.codec.http.HttpObject)} method.
     */
    private volatile HttpRequest capturedOriginalRequest;

    /**
     * True if this filter instance processed a {@link #proxyToServerResolutionSucceeded(String, InetSocketAddress)} call, indicating
     * that the hostname was resolved and populated in the HAR (if this is not a CONNECT).
     */
    private volatile boolean addressResolved = false;

    /**
     * Create a new instance of the HarCaptureFilter that will capture request and response information. If no har is specified in the
     * constructor, this filter will do nothing.
     * <p/>
     * Regardless of the CaptureTypes specified in <code>dataToCapture</code>, the HarCaptureFilter will always capture:
     * <ul>
     * <li>Request and response sizes</li>
     * <li>HTTP request and status lines</li>
     * <li>Page timing information</li>
     * </ul>
     *
     * @param originalRequest the original HttpRequest from the HttpFiltersSource factory
     * @param har             a reference to the ProxyServer's current HAR file at the time this request is received (can be null if HAR capture is not required)
     * @param currentPageRef  the ProxyServer's currentPageRef at the time this request is received from the client
     * @param dataToCapture   the data types to capture for this request. null or empty set indicates only basic information will be
     *                        captured (see {@link net.lightbody.bmp.proxy.CaptureType} for information on data collected for each CaptureType)
     */
    public HarCaptureFilter(HttpRequest originalRequest, ChannelHandlerContext ctx, Har har, String currentPageRef, Set<CaptureType> dataToCapture) {
        super(originalRequest, ctx);

        if (har == null) {
            throw new IllegalStateException("Attempted har capture when har is null");
        }

        if (ProxyUtils.isCONNECT(originalRequest)) {
            throw new IllegalStateException("Attempted har capture for HTTP CONNECT request");
        }

        this.clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();

        if (dataToCapture != null && !dataToCapture.isEmpty()) {
            this.dataToCapture = EnumSet.copyOf(dataToCapture);
        } else {
            this.dataToCapture = EnumSet.noneOf(CaptureType.class);
        }

        // we may need to capture both the request and the response, so set up the request/response filters and delegate to them when
        // the corresponding filter methods are invoked. to save time and memory, only set up the capturing filters when
        // we actually need to capture the data.
        if (this.dataToCapture.contains(CaptureType.REQUEST_CONTENT) || this.dataToCapture.contains(CaptureType.REQUEST_BINARY_CONTENT)) {
            requestCaptureFilter = new ClientRequestCaptureFilter(originalRequest);
        } else {
            requestCaptureFilter = null;
        }

        if (this.dataToCapture.contains(CaptureType.RESPONSE_CONTENT) || this.dataToCapture.contains(CaptureType.RESPONSE_BINARY_CONTENT)) {
            responseCaptureFilter = new ServerResponseCaptureFilter(originalRequest, true);
        } else {
            responseCaptureFilter = null;
        }

        this.har = har;

        this.proxyManager = ProxyManager.newInstance();
        this.harEntry = new HarEntry(currentPageRef);
        harEntry.setId(proxyManager.getDCRequestId());
        this.harRequest = new DCRequest(harEntry);
        this.harRequest.attachBodyHelper(this.proxyManager.getRequestBodyHelper());
        this.harResponse = new DCResponse(harEntry);
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        // if a ServerResponseCaptureFilter is configured, delegate to it to collect the client request. if it is not
        // configured, we still need to capture basic information (timings, possibly client headers, etc.), just not content.
        Log.e("Capture", "clientToProxyRequest " + harEntry.getId());
        if (requestCaptureFilter != null) {
            requestCaptureFilter.clientToProxyRequest(httpObject);
        }

        if (httpObject instanceof HttpRequest) {
            // link the object up now, before we make the request, so that if we get cut off (ie: favicon.ico request and browser shuts down)
            // we still have the attempt associated, even if we never got a response
            harEntry.setStartedDateTime(new Date());
            har.getLog().addEntry(harEntry);

            HttpRequest httpRequest = (HttpRequest) httpObject;
            this.capturedOriginalRequest = httpRequest;

            // associate this request's DCRequest object with the har entry
            HarRequest request = createHarRequestForHttpRequest(httpRequest);
            harEntry.setRequest(request);

            // create a "no response received" DCResponse, in case the connection is interrupted, terminated, or the response is not received
            // for any other reason. having a "default" DCResponse prevents us from generating an invalid HAR.
            HarResponse defaultHarResponse = HarCaptureUtil.createHarResponseForFailure();
            defaultHarResponse.setError(HarCaptureUtil.getNoResponseReceivedErrorMessage());
            harEntry.setResponse(defaultHarResponse);

            captureQueryParameters(httpRequest);
            // not capturing user agent: in many cases, it doesn't make sense to capture at the HarLog level, since the proxy could be
            // serving requests from many different clients with various user agents. clients can turn on the REQUEST_HEADERS capture type
            // in order to capture the User-Agent header, if desired.
            captureRequestHeaderSize(httpRequest);

            if (dataToCapture.contains(CaptureType.REQUEST_COOKIES)) {
                captureRequestCookies(httpRequest);
            }

            if (dataToCapture.contains(CaptureType.REQUEST_HEADERS)) {
                captureRequestHeaders(httpRequest);
            }

            // The HTTP CONNECT to the proxy server establishes the SSL connection to the remote server, but the
            // HTTP CONNECT is not recorded in a separate HarEntry (except in case of error). Instead, the ssl and
            // connect times are recorded in the first request between the client and remote server after the HTTP CONNECT.
            captureConnectTiming();
        }

        if (httpObject instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) httpObject;

            captureRequestSize(httpContent);
        }

        if (httpObject instanceof LastHttpContent) {
            LastHttpContent lastHttpContent = (LastHttpContent) httpObject;
            if (dataToCapture.contains(CaptureType.REQUEST_HEADERS)) {
                captureTrailingHeaders(lastHttpContent);
            }

            if (dataToCapture.contains(CaptureType.REQUEST_CONTENT)) {
                captureRequestContent(requestCaptureFilter.getHttpRequest(), requestCaptureFilter.getFullRequestContents());
            }

            harRequest.getRequest().setBodySize(requestBodySize.get());
        }

        return null;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        // if a ServerResponseCaptureFilter is configured, delegate to it to collect the server's response. if it is not
        // configured, we still need to capture basic information (timings, HTTP status, etc.), just not content.
        Log.e("Capture", "serverToProxyResponse " + harEntry.getId());
        if (responseCaptureFilter != null) {
            responseCaptureFilter.serverToProxyResponse(httpObject);
        }

        if (httpObject instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) httpObject;

            captureResponse(httpResponse);
        }

        if (httpObject instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) httpObject;

            captureResponseSize(httpContent);
        }

        if (httpObject instanceof LastHttpContent) {
            if (dataToCapture.contains(CaptureType.RESPONSE_CONTENT)) {
                captureResponseContent(responseCaptureFilter.getHttpResponse(), responseCaptureFilter.getFullResponseContents());
            }

            harResponse.getResponse().setBodySize(responseBodySize.get());
        }
        proxyManager.responseHeadersReceived(harResponse);

        return super.serverToProxyResponse(httpObject);
    }

    @Override
    public void serverToProxyResponseTimedOut() {
        // replace any existing DCResponse that was created if the server sent a partial response
        Log.e("Capture", "serverToProxyResponseTimedOut " + harEntry.getId());
        HarResponse response = HarCaptureUtil.createHarResponseForFailure();
        harEntry.setResponse(response);

        response.setError(HarCaptureUtil.getResponseTimedOutErrorMessage());
        proxyManager.httpExchangeFailed(response.getError());


        // include this timeout time in the HarTimings object
        long timeoutTimestampNanos = System.nanoTime();

        // if the proxy started to send the request but has not yet finished, we are currently "sending"
        if (sendStartedNanos > 0L && sendFinishedNanos == 0L) {
            harEntry.getTimings().setSend(timeoutTimestampNanos - sendStartedNanos, TimeUnit.NANOSECONDS);
        }
        // if the entire request was sent but the proxy has not begun receiving the response, we are currently "waiting"
        else if (sendFinishedNanos > 0L && responseReceiveStartedNanos == 0L) {
            harEntry.getTimings().setWait(timeoutTimestampNanos - sendFinishedNanos, TimeUnit.NANOSECONDS);
        }
        // if the proxy has already begun to receive the response, we are currenting "receiving"
        else if (responseReceiveStartedNanos > 0L) {
            harEntry.getTimings().setReceive(timeoutTimestampNanos - responseReceiveStartedNanos, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Creates a DCRequest object using the method, url, and HTTP version of the specified request.
     *
     * @param httpRequest HTTP request on which the DCRequest will be based
     * @return a new DCRequest object
     */
    private HarRequest createHarRequestForHttpRequest(HttpRequest httpRequest) {
        // the HAR spec defines the request.url field as:
        //     url [string] - Absolute URL of the request (fragments are not included).
        // the URI on the httpRequest may only identify the path of the resource, so find the full URL.
        // the full URL consists of the scheme + host + port (if non-standard) + path + query params + fragment.
        String url = getFullUrl(httpRequest);

        return new HarRequest(httpRequest.getMethod().toString(), url, httpRequest.getProtocolVersion().text());
    }

    //TODO: add unit tests for these utility-like capture() methods

    protected void captureQueryParameters(HttpRequest httpRequest) {
        // capture query parameters. it is safe to assume the query string is UTF-8, since it "should" be in US-ASCII (a subset of UTF-8),
        // but sometimes does include UTF-8 characters.
        Log.e("InnerHandle", "captureQueryParameters " + harEntry.getId());
        QueryStringDecoder queryStringDecoder = null;
        queryStringDecoder = new QueryStringDecoder(httpRequest.getUri(), Charset.forName("UTF-8"));

        try {
            for (Map.Entry<String, List<String>> entry : queryStringDecoder.parameters().entrySet()) {
                for (String value : entry.getValue()) {
                    harEntry.getRequest().getQueryString().add(new HarNameValuePair(entry.getKey(), value));
                }
            }
        } catch (IllegalArgumentException e) {
            // QueryStringDecoder will throw an IllegalArgumentException if it cannot interpret a query string. rather than cause the entire request to
            // fail by propagating the exception, simply skip the query parameter capture.
            harEntry.setComment("Unable to decode query parameters on URI: " + httpRequest.getUri());
            log.info("Unable to decode query parameters on URI: " + httpRequest.getUri(), e);
        }
    }

    protected void captureRequestHeaderSize(HttpRequest httpRequest) {
        Log.e("InnerHandle", "captureRequestHeaderSize " + harEntry.getId());
        String requestLine = httpRequest.getMethod().toString() + ' ' + httpRequest.getUri() + ' ' + httpRequest.getProtocolVersion().toString();
        // +2 => CRLF after status line, +4 => header/data separation
        long requestHeadersSize = requestLine.length() + 6;

        HttpHeaders headers = httpRequest.headers();
        requestHeadersSize += BrowserMobHttpUtil.getHeaderSize(headers);

        harRequest.getRequest().setHeadersSize(requestHeadersSize);
    }

    protected void captureRequestCookies(HttpRequest httpRequest) {
        Log.e("InnerHandle", "captureRequestCookies " + harEntry.getId());
        String cookieHeader = httpRequest.headers().get(HttpHeaders.Names.COOKIE);
        if (cookieHeader == null) {
            return;
        }

        Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieHeader);

        for (Cookie cookie : cookies) {
            HarCookie harCookie = new HarCookie();

            harCookie.setName(cookie.name());
            harCookie.setValue(cookie.value());

            harRequest.getRequest().getCookies().add(harCookie);
            harRequest.addHeader(cookie.name(), cookie.value());
        }
    }

    protected void captureRequestHeaders(HttpRequest httpRequest) {
        Log.e("InnerHandle", "captureRequestHeaders " + harEntry.getId());
        HttpHeaders headers = httpRequest.headers();

        captureHeaders(headers);
    }

    protected void captureTrailingHeaders(LastHttpContent lastHttpContent) {
        Log.e("InnerHandle", "captureTrailingHeaders " + harEntry.getId());
        HttpHeaders headers = lastHttpContent.trailingHeaders();

        captureHeaders(headers);
    }

    protected void captureHeaders(HttpHeaders headers) {
        Log.e("InnerHandle", "captureHeaders " + harEntry.getId());
        for (Map.Entry<String, String> header : headers.entries()) {
            harRequest.getRequest().getHeaders().add(new HarNameValuePair(header.getKey(), header.getValue()));
            harRequest.addHeader(header.getKey(), header.getValue());
        }
    }

    protected void captureRequestContent(HttpRequest httpRequest, byte[] fullMessage) {
        Log.e("InnerHandle", "captureRequestContent " + harEntry.getId());
        if (fullMessage.length == 0) {
            harRequest.getRequest().getContent().setText("Empty body");
            return;
        }

        harRequest.getRequest().getContent().setBinaryContent(fullMessage);
        harRequest.getRequest().getContent().setText(fullMessage.length + " bytes");

        String contentType = HttpHeaders.getHeader(httpRequest, HttpHeaders.Names.CONTENT_TYPE);
        if (contentType == null) {
            log.warn("No content type specified in request to {}. Content will be treated as {}", httpRequest.getUri(), BrowserMobHttpUtil.UNKNOWN_CONTENT_TYPE);
            contentType = BrowserMobHttpUtil.UNKNOWN_CONTENT_TYPE;
        }

        HarPostData postData = new HarPostData();
        harRequest.getRequest().setPostData(postData);

        postData.setMimeType(contentType);

        boolean urlEncoded;
        if (contentType.startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED)) {
            urlEncoded = true;
        } else {
            urlEncoded = false;
        }

        Charset charset;
        try {
            charset = BrowserMobHttpUtil.readCharsetInContentTypeHeader(contentType);
        } catch (UnsupportedCharsetException e) {
            log.warn("Found unsupported character set in Content-Type header '{}' in HTTP request to {}. Content will not be captured in HAR.", contentType, httpRequest.getUri(), e);
            return;
        }

        if (charset == null) {
            // no charset specified, so use the default -- but log a message since this might not encode the data correctly
            charset = BrowserMobHttpUtil.DEFAULT_HTTP_CHARSET;
            log.debug("No charset specified; using charset {} to decode contents to {}", charset, httpRequest.getUri());
        }

        if (urlEncoded) {
            String textContents = BrowserMobHttpUtil.getContentAsString(fullMessage, charset);

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(textContents, charset, false);

            ImmutableList.Builder<HarPostDataParam> paramBuilder = ImmutableList.builder();

            for (Map.Entry<String, List<String>> entry : queryStringDecoder.parameters().entrySet()) {
                for (String value : entry.getValue()) {
                    paramBuilder.add(new HarPostDataParam(entry.getKey(), value));
                }
            }

            harRequest.getRequest().getPostData().setParams(paramBuilder.build());
        } else {
            //TODO: implement capture of files and multipart form data

            // not URL encoded, so let's grab the body of the POST and capture that
            String postBody = BrowserMobHttpUtil.getContentAsString(fullMessage, charset);
            harRequest.getRequest().getPostData().setText(postBody);
        }
    }

    protected void captureResponseContent(HttpResponse httpResponse, byte[] fullMessage) {
        // force binary if the content encoding is not supported
        Log.e("InnerHandle", "captureResponseContent " + harEntry.getId());
        boolean forceBinary = false;

        if (fullMessage.length != 0) {
            harResponse.getResponse().getContent().setBinaryContent(fullMessage);
        }

        String contentType = HttpHeaders.getHeader(httpResponse, HttpHeaders.Names.CONTENT_TYPE);
        if (contentType == null) {
            log.warn("No content type specified in response from {}. Content will be treated as {}", originalRequest.getUri(), BrowserMobHttpUtil.UNKNOWN_CONTENT_TYPE);
            contentType = BrowserMobHttpUtil.UNKNOWN_CONTENT_TYPE;
        }

        if (responseCaptureFilter.isResponseCompressed() && !responseCaptureFilter.isDecompressionSuccessful()) {
            log.warn("Unable to decompress content with encoding: {}. Contents will be encoded as base64 binary data.", responseCaptureFilter.getContentEncoding());

            forceBinary = true;
        }

        Charset charset;
        try {
            charset = BrowserMobHttpUtil.readCharsetInContentTypeHeader(contentType);
        } catch (UnsupportedCharsetException e) {
            log.warn("Found unsupported character set in Content-Type header '{}' in HTTP response from {}. Content will not be captured in HAR.", contentType, originalRequest.getUri(), e);
            return;
        }

        if (charset == null) {
            // no charset specified, so use the default -- but log a message since this might not encode the data correctly
            charset = BrowserMobHttpUtil.DEFAULT_HTTP_CHARSET;
            log.debug("No charset specified; using charset {} to decode contents from {}", charset, originalRequest.getUri());
        }

        if (!forceBinary && BrowserMobHttpUtil.hasTextualContent(contentType)) {
            String text = BrowserMobHttpUtil.getContentAsString(fullMessage, charset);
            harResponse.getResponse().getContent().setText(text);
        } else if (dataToCapture.contains(CaptureType.RESPONSE_BINARY_CONTENT)) {
            harResponse.getResponse().getContent().setText("Binary Content " + fullMessage.length + " bytes");
        }

        harResponse.getResponse().getContent().setSize(fullMessage.length);
    }

    protected void captureResponse(HttpResponse httpResponse) {
        Log.e("InnerHandle", "captureResponse " + harEntry.getId());
        HarResponse response = new HarResponse(httpResponse.getStatus().code(), httpResponse.getStatus().reasonPhrase(), httpResponse.getProtocolVersion().text());
        harEntry.setResponse(response);

        captureResponseHeaderSize(httpResponse);

        captureResponseMimeType(httpResponse);

        if (dataToCapture.contains(CaptureType.RESPONSE_COOKIES)) {
            captureResponseCookies(httpResponse);
        }

        if (dataToCapture.contains(CaptureType.RESPONSE_HEADERS)) {
            captureResponseHeaders(httpResponse);
        }

        if (BrowserMobHttpUtil.isRedirect(httpResponse)) {
            captureRedirectUrl(httpResponse);
        }
    }

    protected void captureResponseMimeType(HttpResponse httpResponse) {
        Log.e("InnerHandle", "captureResponseMimeType " + harEntry.getId());
        String contentType = HttpHeaders.getHeader(httpResponse, HttpHeaders.Names.CONTENT_TYPE);
        // don't set the mimeType to null, since mimeType is a required field
        if (contentType != null) {
            harResponse.getResponse().getContent().setMimeType(contentType);
        }
    }

    protected void captureResponseCookies(HttpResponse httpResponse) {
        Log.e("InnerHandle", "captureResponseCookies " + harEntry.getId());
        List<String> setCookieHeaders = httpResponse.headers().getAll(HttpHeaders.Names.SET_COOKIE);
        if (setCookieHeaders == null) {
            return;
        }

        for (String setCookieHeader : setCookieHeaders) {
            Cookie cookie = ClientCookieDecoder.LAX.decode(setCookieHeader);
            if (cookie == null) {
                return;
            }

            HarCookie harCookie = new HarCookie();

            harCookie.setName(cookie.name());
            harCookie.setValue(cookie.value());
            // comment is no longer supported in the netty ClientCookieDecoder
            harCookie.setDomain(cookie.domain());
            harCookie.setHttpOnly(cookie.isHttpOnly());
            harCookie.setPath(cookie.path());
            harCookie.setSecure(cookie.isSecure());
            if (cookie.maxAge() > 0) {
                // use a Calendar with the current timestamp + maxAge seconds. the locale of the calendar is irrelevant,
                // since we are dealing with timestamps.
                Calendar expires = Calendar.getInstance();
                // zero out the milliseconds, since maxAge is in seconds
                expires.set(Calendar.MILLISECOND, 0);
                // we can't use Calendar.add, since that only takes ints. TimeUnit.convert handles second->millisecond
                // overflow reasonably well by returning the result as Long.MAX_VALUE.
                expires.setTimeInMillis(expires.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(cookie.maxAge(), TimeUnit.SECONDS));

                harCookie.setExpires(expires.getTime());
            }

            harResponse.getResponse().getCookies().add(harCookie);
            harResponse.addHeader(harCookie.getName(), harCookie.getValue());
        }
    }

    protected void captureResponseHeaderSize(HttpResponse httpResponse) {
        Log.e("InnerHandle", "captureResponseHeaderSize " + harEntry.getId());
        String statusLine = httpResponse.getProtocolVersion().toString() + ' ' + httpResponse.getStatus().toString();
        // +2 => CRLF after status line, +4 => header/data separation
        long responseHeadersSize = statusLine.length() + 6;
        HttpHeaders headers = httpResponse.headers();
        responseHeadersSize += BrowserMobHttpUtil.getHeaderSize(headers);

        harResponse.getResponse().setHeadersSize(responseHeadersSize);
    }

    protected void captureResponseHeaders(HttpResponse httpResponse) {
        Log.e("InnerHandle", "captureResponseHeaders " + harEntry.getId());
        HttpHeaders headers = httpResponse.headers();
        for (Map.Entry<String, String> header : headers.entries()) {
            harResponse.getResponse().getHeaders().add(new HarNameValuePair(header.getKey(), header.getValue()));
            harResponse.addHeader(header.getKey(), header.getValue());
        }
    }

    protected void captureRedirectUrl(HttpResponse httpResponse) {
        Log.e("InnerHandle", "captureRedirectUrl " + harEntry.getId());
        String locationHeaderValue = HttpHeaders.getHeader(httpResponse, HttpHeaders.Names.LOCATION);
        if (locationHeaderValue != null) {
            harResponse.getResponse().setRedirectURL(locationHeaderValue);
        }
    }

    /**
     * Adds the size of this httpContent to the requestBodySize.
     *
     * @param httpContent HttpContent to size
     */
    protected void captureRequestSize(HttpContent httpContent) {
        Log.e("InnerHandle", "captureRequestSize " + harEntry.getId());
        ByteBuf bufferedContent = httpContent.content();
        int contentSize = bufferedContent.readableBytes();
        requestBodySize.addAndGet(contentSize);
    }

    /**
     * Adds the size of this httpContent to the responseBodySize.
     *
     * @param httpContent HttpContent to size
     */
    protected void captureResponseSize(HttpContent httpContent) {
        Log.e("InnerHandle", "captureResponseSize " + harEntry.getId());
        ByteBuf bufferedContent = httpContent.content();
        int contentSize = bufferedContent.readableBytes();
        responseBodySize.addAndGet(contentSize);
        proxyManager.dataReceived(contentSize);
    }

    /**
     * Populates ssl and connect timing info in the HAR if an entry for this client and server exist in the cache.
     */
    protected void captureConnectTiming() {
        Log.e("InnerHandle", "captureConnectTiming " + harEntry.getId());
        HttpConnectTiming httpConnectTiming = HttpConnectHarCaptureFilter.consumeConnectTimingForConnection(clientAddress);
        if (httpConnectTiming != null) {
            harEntry.getTimings().setSsl(httpConnectTiming.getSslHandshakeTimeNanos(), TimeUnit.NANOSECONDS);
            harEntry.getTimings().setConnect(httpConnectTiming.getConnectTimeNanos(), TimeUnit.NANOSECONDS);
            harEntry.getTimings().setBlocked(httpConnectTiming.getBlockedTimeNanos(), TimeUnit.NANOSECONDS);
            harEntry.getTimings().setDns(httpConnectTiming.getDnsTimeNanos(), TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Populates the serverIpAddress field of the harEntry using the internal hostname->IP address cache.
     *
     * @param httpRequest HTTP request to take the hostname from
     */
    protected void populateAddressFromCache(HttpRequest httpRequest) {
        Log.e("InnerHandle", "populateAddressFromCache " + harEntry.getId());
        String serverHost = getHost(httpRequest);

        if (serverHost != null && !serverHost.isEmpty()) {
            String resolvedAddress = ResolvedHostnameCacheFilter.getPreviouslyResolvedAddressForHost(serverHost);
            if (resolvedAddress != null) {
                harEntry.setServerIPAddress(resolvedAddress);
            } else {
                // the resolvedAddress may be null if the ResolvedHostnameCacheFilter has expired the entry (which is unlikely),
                // or in the far more common case that the proxy is using a chained proxy to connect to connect to the
                // remote host. since the chained proxy handles IP address resolution, the IP address in the HAR must be blank.
                log.trace("Unable to find cached IP address for host: {}. IP address in HAR entry will be blank.", serverHost);
            }
        } else {
            log.warn("Unable to identify host from request uri: {}", httpRequest.getUri());
        }
    }

    @Override
    public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
        Log.e("Capture", "proxyToServerResolutionStarted " + harEntry.getId());
        dnsResolutionStartedNanos = System.nanoTime();

        // resolution started means the connection is no longer queued, so populate 'blocked' time
        if (connectionQueuedNanos > 0L) {
            harEntry.getTimings().setBlocked(dnsResolutionStartedNanos - connectionQueuedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setBlocked(0L, TimeUnit.NANOSECONDS);
        }

        return null;
    }

    @Override
    public void proxyToServerResolutionFailed(String hostAndPort) {
        Log.e("Capture", "proxyToServerResolutionFailed " + harEntry.getId());
        HarResponse response = HarCaptureUtil.createHarResponseForFailure();
        harEntry.setResponse(response);

        response.setError(HarCaptureUtil.getResolutionFailedErrorMessage(hostAndPort));

        // record the amount of time we attempted to resolve the hostname in the HarTimings object
        if (dnsResolutionStartedNanos > 0L) {
            harEntry.getTimings().setDns(System.nanoTime() - dnsResolutionStartedNanos, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void proxyToServerResolutionSucceeded(String serverHostAndPort, InetSocketAddress resolvedRemoteAddress) {
        Log.e("Capture", "proxyToServerResolutionSucceeded " + harEntry.getId());
        long dnsResolutionFinishedNanos = System.nanoTime();

        if (dnsResolutionStartedNanos > 0L) {
            harEntry.getTimings().setDns(dnsResolutionFinishedNanos - dnsResolutionStartedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setDns(0L, TimeUnit.NANOSECONDS);
        }

        // the address *should* always be resolved at this point
        InetAddress resolvedAddress = resolvedRemoteAddress.getAddress();
        if (resolvedAddress != null) {
            addressResolved = true;

            harEntry.setServerIPAddress(resolvedAddress.getHostAddress());
        }
    }

    @Override
    public void proxyToServerConnectionQueued() {
        Log.e("Capture", "proxyToServerConnectionQueued " + harEntry.getId());
        this.connectionQueuedNanos = System.nanoTime();
    }

    @Override
    public void proxyToServerConnectionStarted() {
        Log.e("Capture", "proxyToServerConnectionStarted " + harEntry.getId());
        this.connectionStartedNanos = System.nanoTime();
    }

    @Override
    public void proxyToServerConnectionFailed() {
        Log.e("Capture", "proxyToServerConnectionFailed " + harEntry.getId());
        HarResponse response = HarCaptureUtil.createHarResponseForFailure();
        harEntry.setResponse(response);

        response.setError(HarCaptureUtil.getConnectionFailedErrorMessage());

        // record the amount of time we attempted to connect in the HarTimings object
        if (connectionStartedNanos > 0L) {
            harEntry.getTimings().setConnect(System.nanoTime() - connectionStartedNanos, TimeUnit.NANOSECONDS);
        }
        proxyManager.httpExchangeFailed(response.getError());
    }

    @Override
    public void proxyToServerConnectionSucceeded(ChannelHandlerContext serverCtx) {
        Log.e("Capture", "proxyToServerConnectionSucceeded " + harEntry.getId());
        long connectionSucceededTimeNanos = System.nanoTime();

        // make sure the previous timestamp was captured, to avoid setting an absurd value in the har (see serverToProxyResponseReceiving())
        if (connectionStartedNanos > 0L) {
            harEntry.getTimings().setConnect(connectionSucceededTimeNanos - connectionStartedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setConnect(0L, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void proxyToServerRequestSending() {
        Log.e("Capture", "proxyToServerRequestSending " + harEntry.getId());
        this.sendStartedNanos = System.nanoTime();

        // if the hostname was not resolved (and thus the IP address populated in the har) during this request, populate the IP address from the cache
        if (!addressResolved) {
            populateAddressFromCache(capturedOriginalRequest);
        }
        proxyManager.requestWillBeSent(harRequest);
    }

    @Override
    public void proxyToServerRequestSent() {
        Log.e("Capture", "proxyToServerRequestSent " + harEntry.getId());
        this.sendFinishedNanos = System.nanoTime();

        // make sure the previous timestamp was captured, to avoid setting an absurd value in the har (see serverToProxyResponseReceiving())
        if (sendStartedNanos > 0L) {
            harEntry.getTimings().setSend(sendFinishedNanos - sendStartedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setSend(0L, TimeUnit.NANOSECONDS);
        }
        proxyManager.dataSent(harRequest);
    }

    @Override
    public void serverToProxyResponseReceiving() {
        Log.e("Capture", "serverToProxyResponseReceiving " + harEntry.getId());
        this.responseReceiveStartedNanos = System.nanoTime();

        // started to receive response, so populate the 'wait' time. if we started receiving a response from the server before we finished
        // sending (for example, the server replied with a 404 while we were uploading a large file), there was no wait time, so
        // make sure the wait is set to 0.
        if (sendFinishedNanos > 0L && sendFinishedNanos < responseReceiveStartedNanos) {
            harEntry.getTimings().setWait(responseReceiveStartedNanos - sendFinishedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setWait(0L, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void serverToProxyResponseReceived() {
        Log.e("Capture", "serverToProxyResponseReceived " + harEntry.getId());
        long responseReceivedNanos = System.nanoTime();

        // like the wait time, the receive time requires that the serverToProxyResponseReceiving() method be called before this method is invoked.
        // typically that should happen, but it has been reported (https://github.com/lightbody/browsermob-proxy/issues/288) that it
        // sometimes does not. therefore, to be safe, make sure responseReceiveStartedNanos is populated before setting the receive time.
        if (responseReceiveStartedNanos > 0L) {
            harEntry.getTimings().setReceive(responseReceivedNanos - responseReceiveStartedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setReceive(0L, TimeUnit.NANOSECONDS);
        }
        proxyManager.interpretResponseStream(harResponse);
    }


}
