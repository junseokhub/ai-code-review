package com.junseok.aicodereview.review;

import com.junseok.aicodereview.harness.HarnessClient;
import com.junseok.aicodereview.harness.LLMProvider;
import com.junseok.aicodereview.harness.dto.HarnessRequest;
import com.junseok.aicodereview.harness.dto.HarnessResponse;
import com.junseok.aicodereview.provider.GitProvider;
import com.junseok.aicodereview.provider.GitProviderRegistry;
import com.junseok.aicodereview.provider.GitProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MAX_DIFF_LENGTH = 10000;

    private final GitProviderRegistry GitProviderRegistry;
    private final HarnessClient harnessClient;

    public HarnessResponse reviewPR(LLMProvider llmProvider, GitProviderType providerType, String repoName, int prNumber) {
        GitProvider provider = GitProviderRegistry.getProvider(providerType);

        String diff = provider.getPRDiff(repoName, prNumber);
        diff = truncate(diff);
        log.info("PR 리뷰 시작 - repo: {}, pr: {}", repoName, prNumber);

        HarnessResponse response = harnessClient.review(buildRequest(llmProvider, diff));
        log.info("PR 리뷰 완료 - tokens: {}", response.tokenUsage());

        if (response.success()) {
            provider.postPRComment(repoName, prNumber, response.answer());
        }

        return response;
    }

    public HarnessResponse reviewCommit(LLMProvider llmProvider, GitProviderType providerType, String repoName, String commitSha) {
        GitProvider provider = GitProviderRegistry.getProvider(providerType);

        String diff = provider.getCommitDiff(repoName, commitSha);
        diff = truncate(diff);
        log.info("커밋 리뷰 시작 - repo: {}, sha: {}", repoName, commitSha);

        HarnessResponse response = harnessClient.review(buildRequest(llmProvider, diff));
        log.info("커밋 리뷰 완료 - tokens: {}", response.tokenUsage());

        if (response.success()) {
            provider.postCommitComment(repoName, commitSha, response.answer());
        }

        return response;
    }

    private String truncate(String diff) {
        if (diff.length() > MAX_DIFF_LENGTH) {
            log.warn("diff가 너무 큼, {}자로 자름", MAX_DIFF_LENGTH);
            return diff.substring(0, MAX_DIFF_LENGTH);
        }
        return diff;
    }

    private HarnessRequest buildRequest(LLMProvider llmProvider, String diff) {
        return HarnessRequest.builder()
                .provider(llmProvider)
                .topic("코드 리뷰")
                .topicDesc("코드 품질, 보안, 성능, 가독성에 관한 리뷰")
                .query("아래는 git diff 형식의 코드 변경사항입니다. 코드 품질, 보안, 성능, 가독성 관점에서 리뷰해주세요.")
                .context(diff)
                .build();
    }
}