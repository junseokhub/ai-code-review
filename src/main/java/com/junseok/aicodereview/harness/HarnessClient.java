package com.junseok.aicodereview.harness;

import com.junseok.aicodereview.harness.dto.HarnessRequest;
import com.junseok.aicodereview.harness.dto.HarnessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HarnessClient {

    private final WebClient webClient;

    public HarnessClient(HarnessProperties properties, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(properties.url())
                .build();
    }

    public HarnessResponse review(HarnessRequest request) {
        return webClient.post()
                .uri("/api/v1/harness/run")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class)
                                .doOnNext(body -> log.error("하네스 에러 응답: {}", body))
                                .flatMap(body -> Mono.error(new RuntimeException("하네스 에러: " + body)))
                )
                .bodyToMono(HarnessResponse.class)
                .block();
    }
}