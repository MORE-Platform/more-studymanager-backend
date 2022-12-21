package io.redlink.more.studymanager.controller.proxy;

import io.redlink.more.studymanager.properties.KibanaProperties;
import io.redlink.more.studymanager.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(value = "/kibana")
@EnableConfigurationProperties(KibanaProperties.class)
public class KibanaProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(KibanaProxy.class);

    private final ProxyService proxyService;

    private final URI baseUri;

    public KibanaProxy(ProxyService proxyService, KibanaProperties kibanaProperties) {
        this.proxyService = proxyService;
        this.baseUri = kibanaProperties.uri();
    }

    @RequestMapping(value = "/{*path}")
    public ResponseEntity proxy(
            @PathVariable final String path,
            final HttpServletRequest request) {
        return proxyService.processProxyRequest(request, baseUri, "kibana/"+path);
    }
}
