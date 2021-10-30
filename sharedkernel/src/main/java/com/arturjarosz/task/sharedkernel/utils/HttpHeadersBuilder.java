package com.arturjarosz.task.sharedkernel.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class HttpHeadersBuilder {
    private final HttpHeaders headers;
    private final HttpHeadersBuilder headersBuilder;

    public HttpHeadersBuilder() {
        this.headers = new HttpHeaders();
        this.headersBuilder = this;
    }

    public HttpHeadersBuilder withLocation(String path, Object... variables) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance().path(path).buildAndExpand(variables);
        this.headers.setLocation(uriComponents.toUri());
        return this;
    }

    public HttpHeaders build() {
        return this.headers;
    }
}
