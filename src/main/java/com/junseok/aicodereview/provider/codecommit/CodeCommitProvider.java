package com.junseok.aicodereview.provider.codecommit;

import com.junseok.aicodereview.provider.GitProvider;
import com.junseok.aicodereview.provider.GitProviderProperties;
import com.junseok.aicodereview.provider.GitProviderType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.codecommit.CodeCommitClient;
import software.amazon.awssdk.services.codecommit.model.*;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "git.provider", name = "codecommit-access-key", matchIfMissing = false)
public class CodeCommitProvider implements GitProvider {

    private final CodeCommitClient codeCommitClient;

    public CodeCommitProvider(GitProviderProperties properties) {
        this.codeCommitClient = CodeCommitClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                properties.codecommitAccessKey(),
                                properties.codecommitSecretKey()
                        )
                ))
                .region(Region.AP_NORTHEAST_2)
                .build();
    }

    @Override
    public String getPRDiff(String repoName, int prNumber) {
        GetPullRequestResponse pr = codeCommitClient.getPullRequest(
                GetPullRequestRequest.builder()
                        .pullRequestId(String.valueOf(prNumber))
                        .build()
        );

        String beforeCommit = pr.pullRequest().pullRequestTargets().get(0).mergeBase();
        String afterCommit = pr.pullRequest().pullRequestTargets().get(0).sourceCommit();

        return buildDiff(getDiffs(repoName, beforeCommit, afterCommit));
    }

    @Override
    public void postPRComment(String repoName, int prNumber, String comment) {
        codeCommitClient.postCommentForPullRequest(
                PostCommentForPullRequestRequest.builder()
                        .pullRequestId(String.valueOf(prNumber))
                        .repositoryName(repoName)
                        .content(comment)
                        .build()
        );
    }

    @Override
    public String getCommitDiff(String repoName, String commitSha) {
        GetCommitResponse commit = codeCommitClient.getCommit(
                GetCommitRequest.builder()
                        .repositoryName(repoName)
                        .commitId(commitSha)
                        .build()
        );
        String parentCommit = commit.commit().parents().get(0);
        return buildDiff(getDiffs(repoName, parentCommit, commitSha));
    }

    @Override
    public void postCommitComment(String repoName, String commitSha, String comment) {
        codeCommitClient.postCommentForComparedCommit(
                PostCommentForComparedCommitRequest.builder()
                        .repositoryName(repoName)
                        .afterCommitId(commitSha)
                        .content(comment)
                        .build()
        );
    }

    @Override
    public List<String> getPRFileDiffs(String repoName, int prNumber) {
        GetPullRequestResponse pr = codeCommitClient.getPullRequest(
                GetPullRequestRequest.builder()
                        .pullRequestId(String.valueOf(prNumber))
                        .build()
        );
        String beforeCommit = pr.pullRequest().pullRequestTargets().get(0).mergeBase();
        String afterCommit = pr.pullRequest().pullRequestTargets().get(0).sourceCommit();
        return getDiffs(repoName, beforeCommit, afterCommit);
    }

    @Override
    public List<String> getCommitFileDiffs(String repoName, String commitSha) {
        GetCommitResponse commit = codeCommitClient.getCommit(
                GetCommitRequest.builder()
                        .repositoryName(repoName)
                        .commitId(commitSha)
                        .build()
        );
        String parentCommit = commit.commit().parents().get(0);
        return getDiffs(repoName, parentCommit, commitSha);
    }

    @Override
    public GitProviderType getProviderType() {
        return GitProviderType.CODECOMMIT;
    }

    private List<String> getDiffs(String repoName, String beforeCommit, String afterCommit) {
        return codeCommitClient.getDifferences(
                        GetDifferencesRequest.builder()
                                .repositoryName(repoName)
                                .beforeCommitSpecifier(beforeCommit)
                                .afterCommitSpecifier(afterCommit)
                                .build()
                )
                .differences()
                .stream()
                .map(d -> "파일: " + d.afterBlob().path() + "\n" + d.afterBlob().mode())
                .toList();
    }

    private String buildDiff(List<String> entries) {
        return String.join("\n\n", entries);
    }
}