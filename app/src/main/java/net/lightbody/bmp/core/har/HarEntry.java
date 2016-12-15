package net.lightbody.bmp.core.har;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.lightbody.bmp.core.json.ISO8601WithTDZDateFormatter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import me.moxun.dreamcatcher.connector.reporter.NetworkEventReporterImpl;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect
public class HarEntry {
    private volatile String id;
    private volatile String pageref;
    private volatile Date startedDateTime;
    private volatile HarRequest request;
    private volatile HarResponse response;
    private volatile HarCache cache = new HarCache();
    private volatile HarTimings timings = new HarTimings();
    private volatile String serverIPAddress;
    private volatile String connection;
    private volatile String comment = "";
    private volatile double requestTime;

    public HarEntry(String pageref) {
        this.pageref = pageref;
    }

    public double getRequestTime() {
        return requestTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getPageref() {
        return pageref;
    }

    public void setPageref(String pageref) {
        this.pageref = pageref;
    }

    @JsonSerialize(using = ISO8601WithTDZDateFormatter.class)
    public Date getStartedDateTime() {
        return startedDateTime;
    }

    public void setStartedDateTime(Date startedDateTime) {
        this.startedDateTime = startedDateTime;
        this.requestTime = NetworkEventReporterImpl.now();
    }

    /**
     * Retrieves the time for this HarEntry in milliseconds. To retrieve the time in another time unit, use {@link #getTime(TimeUnit)}.
     * Rather than storing the time directly, calculate the time from the HarTimings as required in the HAR spec.
     * From <a href="https://dvcs.w3.org/hg/webperf/raw-file/tip/specs/HAR/Overview.html">https://dvcs.w3.org/hg/webperf/raw-file/tip/specs/HAR/Overview.html</a>,
     * section <code>4.2.16 timings</code>:
     <pre>
     Following must be true in case there are no -1 values (entry is an object in log.entries) :

     entry.time == entry.timings.blocked + entry.timings.dns +
     entry.timings.connect + entry.timings.send + entry.timings.wait +
     entry.timings.receive;
     </pre>
     * @return time for this HAR entry, in milliseconds
     */
    public long getTime() {
        return getTime(TimeUnit.MILLISECONDS);
    }

    /**
     * Retrieve the time for this HarEntry in the specified timeUnit. See {@link #getTime()} for details.
     *
     * @param timeUnit units of time to return
     * @return time for this har entry
     */
    public long getTime(TimeUnit timeUnit) {
        HarTimings timings = getTimings();
        if (timings == null) {
            return -1;
        }

        long timeNanos = 0;
        if (timings.getBlocked(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getBlocked(TimeUnit.NANOSECONDS);
        }

        if (timings.getDns(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getDns(TimeUnit.NANOSECONDS);
        }

        if (timings.getConnect(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getConnect(TimeUnit.NANOSECONDS);
        }

        if (timings.getSend(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getSend(TimeUnit.NANOSECONDS);
        }

        if (timings.getWait(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getWait(TimeUnit.NANOSECONDS);
        }

        if (timings.getReceive(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getReceive(TimeUnit.NANOSECONDS);
        }

        return timeUnit.convert(timeNanos, TimeUnit.NANOSECONDS);
    }

    public HarRequest getRequest() {
        return request;
    }

    public void setRequest(HarRequest request) {
        this.request = request;
    }

    public HarResponse getResponse() {
        return response;
    }

    public void setResponse(HarResponse response) {
        this.response = response;
    }

    public HarCache getCache() {
        return cache;
    }

    public void setCache(HarCache cache) {
        this.cache = cache;
    }

    public HarTimings getTimings() {
        return timings;
    }

    public void setTimings(HarTimings timings) {
        this.timings = timings;
    }

    public String getServerIPAddress() {
        return serverIPAddress;
    }

    public void setServerIPAddress(String serverIPAddress) {
        this.serverIPAddress = serverIPAddress;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HarEntry{");
        sb.append("cache=").append(cache);
        sb.append(", pageref='").append(pageref).append('\'');
        sb.append(", startedDateTime=").append(startedDateTime);
        sb.append(", request=").append(request);
        sb.append(", response=").append(response);
        sb.append(", timings=").append(timings);
        sb.append(", serverIPAddress='").append(serverIPAddress).append('\'');
        sb.append(", connection='").append(connection).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
