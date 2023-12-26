package com.arturjarosz.task.sharedkernel.testhelpers;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class HttpHeadersBuilder {
    private final HttpHeaders headers;

    public HttpHeadersBuilder() {
        this.headers = new HttpHeaders();
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
