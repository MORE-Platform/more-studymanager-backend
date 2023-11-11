/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.service;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

//inspired ;) by from https://blogs.ashrithgn.com/writing-a-reverse-proxy-service-in-spring-boot/
@Service
public class ProxyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyService.class);

    private final ClientHttpRequestFactory factory;

    public ProxyService() {
        factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    }

    //maybe add some retry
    public ResponseEntity<Resource> processProxyRequest(HttpServletRequest request,
                                                      URI basePath,
                                                      String path) {

        // replacing context path form urI to match actual gateway URI
        URI uri = UriComponentsBuilder.fromUri(basePath)
                .path(path)
                .query(request.getQueryString())
                .build(true).toUri();

        HttpHeaders requestHeaders = getRequestHeaders(request);

        try {

            String requestBody = IOUtils.toString(request.getReader());
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, requestHeaders);

            RestTemplate restTemplate = new RestTemplate(factory);

            ResponseEntity<Resource> serverResponse = restTemplate.exchange(
                    uri, HttpMethod.valueOf(request.getMethod()), httpEntity, Resource.class
            );

            return new ResponseEntity<>(
                    serverResponse.getBody(),
                    getResponseHeaders(getResponseHeaders(serverResponse.getHeaders())),
                    serverResponse.getStatusCode()
            );


        } catch (HttpStatusCodeException e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(e.getStatusCode().value()).build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }

    }

    private HttpHeaders getResponseHeaders(HttpHeaders headers) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.addAll(headers);
        return responseHeaders;
    }

    private static HttpHeaders getRequestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }
        return headers;
    }
}
