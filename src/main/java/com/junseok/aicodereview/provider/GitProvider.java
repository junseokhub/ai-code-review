package com.junseok.aicodereview.provider;

import java.util.List;

public interface GitProvider {
    // PR 관련
    String getPRDiff(String repoName, int prNumber);
    void postPRComment(String repoName, int prNumber, String comment);

    // 커밋 관련
    String getCommitDiff(String repoName, String commitSha);
    void postCommitComment(String repoName, String commitSha, String comment);

    // provider 이름
    GitProviderType getProviderType();

    List<String> getPRFileDiffs(String repoName, int prNumber);
    List<String> getCommitFileDiffs(String repoName, String commitSha);
}
