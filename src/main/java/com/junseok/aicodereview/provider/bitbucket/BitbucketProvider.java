package com.junseok.aicodereview.provider.bitbucket;

import com.junseok.aicodereview.provider.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "git.provider", name = "bitbucket-token")
public class BitbucketProvider implements GitProvider {

    private final WebClient webClient;

    public BitbucketProvider(GitProviderProperties properties, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.bitbucket.org/2.0")
                .defaultHeader("Authorization", "Bearer " + properties.bitbucketToken())
                .build();
    }

    @Override
    public String getPRDiff(String repoName, int prNumber) {
        String diff = webClient.get()
                .uri("/repositories/{repoName}/pullrequests/{prNumber}/diff", repoName, prNumber)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return diff;
    }

    @Override
    public void postPRComment(String repoName, int prNumber, String comment) {
        webClient.post()
                .uri("/repositories/{repoName}/pullrequests/{prNumber}/comments", repoName, prNumber)
                .bodyValue(Map.of("content", Map.of("raw", comment)))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public String getCommitDiff(String repoName, String commitSha) {
        String diff = webClient.get()
                .uri("/repositories/{repoName}/diff/{commitSha}", repoName, commitSha)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return diff;
    }

    @Override
    public void postCommitComment(String repoName, String commitSha, String comment) {
        webClient.post()
                .uri("/repositories/{repoName}/commit/{commitSha}/comments", repoName, commitSha)
                .bodyValue(Map.of("content", Map.of("raw", comment)))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public List<String> getPRFileDiffs(String repoName, int prNumber) {
        String diff = getPRDiff(repoName, prNumber);
        return parseDiffToFiles(diff);
    }

    @Override
    public List<String> getCommitFileDiffs(String repoName, String commitSha) {
        String diff = getCommitDiff(repoName, commitSha);
        return parseDiffToFiles(diff);
    }

    @Override
    public GitProviderType getProviderType() {
        return GitProviderType.BITBUCKET;
    }

    private List<String> parseDiffToFiles(String diff) {
        if (diff == null) return List.of();
        return List.of(diff.split("(?=diff --git)"));
    }
}