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

import java.util.List;

import static com.junseok.aicodereview.config.FileFilterUtils.shouldSkip;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSeparateFileService {

    private final GitProviderRegistry GitProviderRegistry;
    private final HarnessClient harnessClient;
    private final ReviewProperties reviewProperties;

    public List<HarnessResponse> reviewPR(LLMProvider llmProvider, GitProviderType providerType, String repoName, int prNumber) {
        GitProvider provider = GitProviderRegistry.getProvider(providerType);

        return provider.getPRFileDiffs(repoName, prNumber).stream()
                .filter(diff -> {
                    String fileName = extractFileName(diff);
                    if (shouldSkip(fileName, reviewProperties.skipPatterns())) {
                        log.info("스킵: {}", fileName);
                        return false;
                    }
                    return true;
                })
                .map(diff -> {
                    String fileName = extractFileName(diff);
                    log.info("PR 리뷰 시작 - repo: {}, pr: {}, file: {}", repoName, prNumber, fileName);

                    HarnessResponse response = harnessClient.review(buildRequest(llmProvider, diff));
                    log.info("PR 리뷰 완료 - file: {}, tokens: {}", fileName, response.tokenUsage());

                    if (response.success()) {
                        provider.postPRComment(repoName, prNumber, fileName + "\n\n" + response.answer());
                    }
                    return response;
                })
                .toList();
    }

    public List<HarnessResponse> reviewCommit(LLMProvider llmProvider, GitProviderType providerType, String repoName, String commitSha) {
        GitProvider provider = GitProviderRegistry.getProvider(providerType);

        return provider.getCommitFileDiffs(repoName, commitSha).stream()
                .filter(diff -> {
                    String fileName = extractFileName(diff);
                    if (shouldSkip(fileName, reviewProperties.skipPatterns())) {
                        log.info("스킵: {}", fileName);
                        return false;
                    }
                    return true;
                })
                .map(diff -> {
                    String fileName = extractFileName(diff);
                    log.info("커밋 리뷰 시작 - repo: {}, sha: {}, file: {}", repoName, commitSha, fileName);

                    HarnessResponse response = harnessClient.review(buildRequest(llmProvider, diff));
                    log.info("커밋 리뷰 완료 - file: {}, tokens: {}", fileName, response.tokenUsage());

                    if (response.success()) {
                        provider.postCommitComment(repoName, commitSha, fileName + "\n\n" + response.answer());
                    }
                    return response;
                })
                .toList();
    }

    private String extractFileName(String diff) {
        return diff.split("\n")[0].replace("파일: ", "");
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
