package com.junseok.aicodereview.provider.gitlab;

import com.junseok.aicodereview.provider.GitProvider;
import com.junseok.aicodereview.provider.GitProviderProperties;
import com.junseok.aicodereview.provider.GitProviderType;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "git.provider", name = "gitlab-token")
public class GitLabProvider implements GitProvider {

    private final GitLabApi gitLabApi;

    public GitLabProvider(GitProviderProperties properties) {
        this.gitLabApi = new GitLabApi("https://gitlab.com", properties.gitlabToken());
    }

    @Override
    public String getPRDiff(String repoName, int prNumber) {
        try {
            return buildDiff(gitLabApi.getMergeRequestApi()
                    .getMergeRequestChanges(repoName, (long) prNumber)
                    .getChanges()
                    .stream()
                    .map(d -> entry(d.getNewPath(), d.getDiff()))
                    .toList());
        } catch (GitLabApiException e) {
            throw new RuntimeException("GitLab MR diff 가져오기 실패: " + e.getMessage());
        }
    }

    @Override
    public void postPRComment(String repoName, int prNumber, String comment) {
        try {
            gitLabApi.getNotesApi().createMergeRequestNote(repoName, (long) prNumber, comment, null, null);
        } catch (GitLabApiException e) {
            throw new RuntimeException("GitLab MR 코멘트 등록 실패: " + e.getMessage());
        }
    }

    @Override
    public String getCommitDiff(String repoName, String commitSha) {
        try {
            return buildDiff(gitLabApi.getCommitsApi()
                    .getDiff(repoName, commitSha)
                    .stream()
                    .map(d -> entry(d.getNewPath(), d.getDiff()))
                    .toList());
        } catch (GitLabApiException e) {
            throw new RuntimeException("GitLab 커밋 diff 가져오기 실패: " + e.getMessage());
        }
    }

    @Override
    public void postCommitComment(String repoName, String commitSha, String comment) {
        try {
            gitLabApi.getCommitsApi().addComment(repoName, commitSha, comment);
        } catch (GitLabApiException e) {
            throw new RuntimeException("GitLab 커밋 코멘트 등록 실패: " + e.getMessage());
        }
    }

    @Override
    public List<String> getPRFileDiffs(String repoName, int prNumber) {
        try {
            return gitLabApi.getMergeRequestApi()
                    .getMergeRequestChanges(repoName, (long) prNumber)
                    .getChanges()
                    .stream()
                    .map(d -> entry(d.getNewPath(), d.getDiff()))
                    .toList();
        } catch (GitLabApiException e) {
            throw new RuntimeException("GitLab MR 파일 diff 가져오기 실패: " + e.getMessage());
        }
    }

    @Override
    public List<String> getCommitFileDiffs(String repoName, String commitSha) {
        try {
            return gitLabApi.getCommitsApi()
                    .getDiff(repoName, commitSha)
                    .stream()
                    .map(d -> entry(d.getNewPath(), d.getDiff()))
                    .toList();
        } catch (GitLabApiException e) {
            throw new RuntimeException("GitLab 커밋 파일 diff 가져오기 실패: " + e.getMessage());
        }
    }

    @Override
    public GitProviderType getProviderType() {
        return GitProviderType.GITLAB;
    }

    private String entry(String fileName, String patch) {
        return "파일: " + fileName + "\n" + patch;
    }

    private String buildDiff(List<String> entries) {
        return String.join("\n\n", entries);
    }
}