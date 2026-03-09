package com.artisan.gateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Rewrites the "servers" URL in OpenAPI JSON responses so "Try it out" in Swagger UI
 * sends requests through the Gateway instead of directly to backend services.
 */
@Component
public class OpenApiServerRewriteFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (!path.contains("/v3/api-docs")) {
            return chain.filter(exchange);
        }

        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                return DataBufferUtils.join(body)
                        .flatMap(dataBuffer -> {
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);
                            String json = new String(content, StandardCharsets.UTF_8);
                            try {
                                JsonNode root = objectMapper.readTree(json);
                                if (root instanceof ObjectNode obj) {
                                    ArrayNode servers = objectMapper.createArrayNode();
                                    servers.addObject().put("url", "/").put("description", "API Gateway");
                                    obj.set("servers", servers);
                                    byte[] rewritten = objectMapper.writeValueAsBytes(root);
                                    return super.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(rewritten)));
                                }
                            } catch (Exception ignored) {
                                // Fallback to original
                            }
                            return super.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(content)));
                        });
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
