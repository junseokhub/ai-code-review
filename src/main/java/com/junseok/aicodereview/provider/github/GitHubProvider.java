package com.junseok.aicodereview.provider.github;

import com.junseok.aicodereview.provider.GitProvider;
import com.junseok.aicodereview.provider.GitProviderProperties;
import com.junseok.aicodereview.provider.GitProviderType;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

@Component
@ConditionalOnProperty(prefix = "git.provider", name = "github-token")
public class GitHubProvider implements GitProvider {

    private final GitHub gitHub;

    public GitHubProvider(GitProviderProperties properties) throws IOException {
        this.gitHub = new GitHubBuilder()
                .withOAuthToken(properties.githubToken())
                .build();
    }

    @Override
    public String getPRDiff(String repoName, int prNumber) {
        try {
            return buildDiff(getRepository(repoName)
                    .getPullRequest(prNumber)
                    .listFiles()
                    .toList()
                    .stream()
                    .map(f -> entry(f.getFilename(), f.getPatch()))
                    .toList());
        } catch (IOException e) {
            throw new RuntimeException("PR diff 가져오기 실패: " + e.getMessage());
        }
    }

    @Override
    public void postPRComment(String repoName, int prNumber, String comment) {
        try {
            getRepository(repoName).getPullRequest(prNumber).comment(comment);
        } catch (IOException e) {
            throw new RuntimeException("PR 코멘트 등록 실패: " + e.getMessage());
        }
    }

    @Override
    public String getCommitDiff(String repoName, String commitSha) {
        try {
            return buildDiff(getRepository(repoName)
                    .getCommit(commitSha)
                    .listFiles()
                    .toList()
                    .stream()
                    .map(f -> entry(f.getFileName(), f.getPatch()))
                    .toList());
        } catch (IOException e) {
            throw new RuntimeException("커밋 diff 가져오기 실패: " + e.getMessage());
        }
    }

    @Override
    public void postCommitComment(String repoName, String commitSha, String comment) {
        try {
            getRepository(repoName).getCommit(commitSha).createComment(comment);
        } catch (IOException e) {
            throw new RuntimeException("커밋 코멘트 등록 실패: " + e.getMessage());
        }
    }

    @Override
    public List<String> getPRFileDiffs(String repoName, int prNumber) {
        try {
            return StreamSupport.stream(getRepository(repoName)
                            .getPullRequest(prNumber)
                            .listFiles()
                            .spliterator(), false)
                    .map(f -> entry(f.getFilename(), f.getPatch()))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("PR 파일 diff 가져오기 실패: " + e.getMessage());
        }
    }

    @Override
    public List<String> getCommitFileDiffs(String repoName, String commitSha) {
        try {
            return getRepository(repoName)
                    .getCommit(commitSha)
                    .listFiles()
                    .toList()
                    .stream()
                    .map(f -> entry(f.getFileName(), f.getPatch()))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("커밋 파일 diff 가져오기 실패: " + e.getMessage());
        }
    }

    @Override
    public GitProviderType getProviderType() {
        return GitProviderType.GITHUB;
    }

    private GHRepository getRepository(String repoName) throws IOException {
        return gitHub.getRepository(repoName);
    }

    private String entry(String fileName, String patch) {
        return "파일: " + fileName + "\n" + patch;
    }

    private String buildDiff(List<String> entries) {
        return String.join("\n\n", entries);
    }
}