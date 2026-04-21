package com.junseok.aicodereview.webhook;

import com.junseok.aicodereview.harness.dto.HarnessResponse;
import com.junseok.aicodereview.review.ReviewSeparateFileService;
import com.junseok.aicodereview.review.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhook")
public class WebhookController {

    private final ReviewService reviewService;
    private final ReviewSeparateFileService reviewSeparateFileService;

    @PostMapping("/pr")
    public ResponseEntity<HarnessResponse> prReview(@RequestBody WebhookRequest request) {
        log.info("PR 리뷰 요청 - repo: {}, pr: {}", request.repoName(), request.prNumber());
        HarnessResponse response = reviewService.reviewPR(
                request.llmProvider(),
                request.providerType(),
                request.repoName(),
                request.prNumber()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/commit")
    public ResponseEntity<HarnessResponse> commitReview(@RequestBody WebhookRequest request) {
        log.info("커밋 리뷰 요청 - repo: {}, sha: {}", request.repoName(), request.commitSha());
        HarnessResponse response = reviewService.reviewCommit(
                request.llmProvider(),
                request.providerType(),
                request.repoName(),
                request.commitSha()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/file/pr")
    public ResponseEntity<List<HarnessResponse>> prFileReview(@RequestBody WebhookRequest request) {
        List<HarnessResponse> responses = reviewSeparateFileService.reviewPR(
                request.llmProvider(),
                request.providerType(),
                request.repoName(),
                request.prNumber()
        );
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/file/commit")
    public ResponseEntity<List<HarnessResponse>> commitFileReview(@RequestBody WebhookRequest request) {
        List<HarnessResponse> responses = reviewSeparateFileService.reviewCommit(
                request.llmProvider(),
                request.providerType(),
                request.repoName(),
                request.commitSha()
        );
        return ResponseEntity.ok(responses);
    }
}