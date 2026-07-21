package com.biometric.capture.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient visionRestClient(@Value("${vision.service.url}") String visionServiceUrl) {
        // Pinned to HTTP/1.1: the JDK HttpClient defaults to attempting an h2c (HTTP/2
        // cleartext) upgrade on plain http:// URLs, which uvicorn (the vision-service's ASGI
        // server) doesn't support — it silently drops the request body instead of rejecting
        // the upgrade, causing every POST to arrive at FastAPI empty.
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        return RestClient.builder()
                .baseUrl(visionServiceUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
