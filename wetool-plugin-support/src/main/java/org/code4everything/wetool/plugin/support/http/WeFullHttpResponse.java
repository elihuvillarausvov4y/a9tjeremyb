package org.code4everything.wetool.plugin.support.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author pantao
 * @since 2020/12/5
 */
@EqualsAndHashCode(callSuper = true)
public class WeFullHttpResponse extends DefaultFullHttpResponse {

    @Getter
    private ByteBuf content;

    public WeFullHttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public void setContent(ByteBuf content) {
        this.content = content;
    }

    public void setContent(String content) {
        setContent(Https.str2buf(content));
    }

    @Override
    public ByteBuf content() {
        return getContent();
    }
}
