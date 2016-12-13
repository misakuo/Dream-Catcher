package me.moxun.dreamcatcher.connector.server.http;

public class LightHttpResponse extends LightHttpMessage {
    public int code;
    public String reasonPhrase;
    public LightHttpBody body;

    @Override
    public String toString() {
        return "LightHttpResponse{" +
                "code=" + code +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                ", body=" + body.toString() +
                '}';
    }

    public void prepare() {
        if (body != null) {
            addHeader(HttpHeaders.CONTENT_TYPE, body.contentType());
            addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.contentLength()));
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.code = -1;
        this.reasonPhrase = null;
        this.body = null;
    }
}
