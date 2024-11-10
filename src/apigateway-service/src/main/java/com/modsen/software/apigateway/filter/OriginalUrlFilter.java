package com.modsen.software.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class OriginalUrlFilter extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Original-Url", request.getURI().toString())
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }
}
