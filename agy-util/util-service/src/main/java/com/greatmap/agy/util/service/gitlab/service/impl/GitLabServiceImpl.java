package com.greatmap.agy.util.service.gitlab.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatmap.agy.util.api.dto.gitlab.GitLabBranchStats;
import com.greatmap.agy.util.api.dto.gitlab.GitLabCommitStats;
import com.greatmap.agy.util.api.dto.gitlab.GitLabProjectStats;
import com.greatmap.agy.util.service.gitlab.service.GitLabService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GitLab 综合统计服务实现
 *
 * @author AGY
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabServiceImpl implements GitLabService {

    private final ObjectMapper objectMapper;

    @Value("${gitlab.url:https://gitlab.example.com/}")
    private String gitlabUrl;

    @Value("${gitlab.token:}")
    private String gitlabToken;

    @Override
    public List<GitLabProjectStats> getAllUserProjectsWithBranches() {
        List<GitLabProjectStats> result = new ArrayList<>();
        List<JsonNode> projects = getCurrentUserProjects();
        for (JsonNode project : projects) {
            try {
                GitLabProjectStats projectStats = buildProjectStats(project);
                result.add(projectStats);
            } catch (Exception e) {
                log.error("统计项目 {} 失败", project.path("name").asText(), e);
            }
        }
        return result;
    }

    @Override
    public GitLabProjectStats getProjectBranchesStats(String projectIdOrPath) {
        JsonNode project = getProject(projectIdOrPath);
        return project != null ? buildProjectStats(project) : null;
    }

    private GitLabProjectStats buildProjectStats(JsonNode project) {
        GitLabProjectStats projectStats = new GitLabProjectStats();
        projectStats.setProjectId(project.path("id").asInt());
        projectStats.setProjectName(project.path("name").asText());
        projectStats.setProjectPath(project.path("path_with_namespace").asText());

        List<GitLabBranchStats> branchStatsList = new ArrayList<>();
        int totalAdditions = 0;
        int totalDeletions = 0;

        List<JsonNode> branches = getProjectBranches(projectStats.getProjectId());
        for (JsonNode branch : branches) {
            try {
                GitLabBranchStats branchStats = buildBranchStats(projectStats.getProjectId(), branch);
                branchStatsList.add(branchStats);
                totalAdditions += branchStats.getTotalAdditions();
                totalDeletions += branchStats.getTotalDeletions();
            } catch (Exception e) {
                log.error("统计分支 {} 失败", branch.path("name").asText(), e);
            }
        }

        projectStats.setBranches(branchStatsList);
        projectStats.setTotalAdditions(totalAdditions);
        projectStats.setTotalDeletions(totalDeletions);
        projectStats.setTotalChanges(totalAdditions + totalDeletions);
        return projectStats;
    }

    private GitLabBranchStats buildBranchStats(Integer projectId, JsonNode branch) {
        GitLabBranchStats branchStats = new GitLabBranchStats();
        branchStats.setBranchName(branch.path("name").asText());

        JsonNode commit = branch.path("commit");
        branchStats.setLastCommitTime(commit.path("committed_date").asText(null));

        List<JsonNode> commits = getBranchCommits(projectId, branchStats.getBranchName());
        List<GitLabCommitStats> commitStatsList = new ArrayList<>();

        int totalAdditions = 0;
        int totalDeletions = 0;

        for (JsonNode c : commits) {
            try {
                GitLabCommitStats commitStats = buildCommitStats(projectId, c);
                commitStatsList.add(commitStats);
                totalAdditions += commitStats.getAdditions();
                totalDeletions += commitStats.getDeletions();
            } catch (Exception e) {
                log.error("统计提交 {} 失败", c.path("id").asText(), e);
            }
        }

        branchStats.setCommits(commitStatsList);
        branchStats.setTotalAdditions(totalAdditions);
        branchStats.setTotalDeletions(totalDeletions);
        branchStats.setTotalChanges(totalAdditions + totalDeletions);
        return branchStats;
    }

    private GitLabCommitStats buildCommitStats(Integer projectId, JsonNode commit) {
        GitLabCommitStats commitStats = new GitLabCommitStats();
        commitStats.setId(commit.path("id").asText());
        commitStats.setMessage(commit.path("message").asText());
        commitStats.setAuthorName(commit.path("author_name").asText());
        commitStats.setCommittedDate(commit.path("committed_date").asText());

        JsonNode stats = commit.path("stats");
        if (!stats.isMissingNode() && !stats.isNull()) {
            commitStats.setAdditions(stats.path("additions").asInt(0));
            commitStats.setDeletions(stats.path("deletions").asInt(0));
            commitStats.setTotalChanges(stats.path("total").asInt(0));
        } else {
            // 获取单独 commit 详情的 stats
            JsonNode detail = getCommitDetail(projectId, commitStats.getId());
            if (detail != null && !detail.path("stats").isMissingNode()) {
                JsonNode dStats = detail.path("stats");
                commitStats.setAdditions(dStats.path("additions").asInt(0));
                commitStats.setDeletions(dStats.path("deletions").asInt(0));
                commitStats.setTotalChanges(dStats.path("total").asInt(0));
            } else {
                commitStats.setAdditions(0);
                commitStats.setDeletions(0);
                commitStats.setTotalChanges(0);
            }
        }
        return commitStats;
    }

    private List<JsonNode> getCurrentUserProjects() {
        if (StringUtils.isBlank(gitlabToken)) {
            return Collections.emptyList();
        }
        List<JsonNode> projects = new ArrayList<>();
        int page = 1;
        try {
            while (true) {
                String baseUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
                String url = baseUrl + "/api/v4/projects?membership=true&per_page=100&page=" + page;
                HttpResponse response = HttpRequest.get(url)
                        .header("PRIVATE-TOKEN", gitlabToken)
                        .timeout(15000)
                        .execute();
                if (response.isOk()) {
                    JsonNode root = objectMapper.readTree(response.body());
                    if (root.isArray() && !root.isEmpty()) {
                        for (JsonNode n : root) {
                            projects.add(n);
                        }
                        page++;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("获取项目列表异常", e);
        }
        return projects;
    }

    private JsonNode getProject(String projectIdOrPath) {
        if (StringUtils.isBlank(gitlabToken)) {
            return null;
        }
        try {
            String baseUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
            String encodedId = URLEncoder.encode(projectIdOrPath, StandardCharsets.UTF_8);
            String url = baseUrl + "/api/v4/projects/" + encodedId;
            HttpResponse response = HttpRequest.get(url)
                    .header("PRIVATE-TOKEN", gitlabToken)
                    .timeout(15000)
                    .execute();
            if (response.isOk()) {
                return objectMapper.readTree(response.body());
            }
        } catch (Exception e) {
            log.error("获取项目 {} 异常", projectIdOrPath, e);
        }
        return null;
    }

    private List<JsonNode> getProjectBranches(Integer projectId) {
        List<JsonNode> branches = new ArrayList<>();
        try {
            String baseUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
            String url = baseUrl + "/api/v4/projects/" + projectId + "/repository/branches?per_page=100";
            HttpResponse response = HttpRequest.get(url)
                    .header("PRIVATE-TOKEN", gitlabToken)
                    .timeout(15000)
                    .execute();
            if (response.isOk()) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.isArray()) {
                    for (JsonNode n : root) {
                        branches.add(n);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取分支异常", e);
        }
        return branches;
    }

    private List<JsonNode> getBranchCommits(Integer projectId, String branchName) {
        List<JsonNode> commits = new ArrayList<>();
        try {
            String baseUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
            String encodedBranch = URLEncoder.encode(branchName, StandardCharsets.UTF_8);
            String url = baseUrl + "/api/v4/projects/" + projectId + "/repository/commits?ref_name=" + encodedBranch + "&per_page=100";
            HttpResponse response = HttpRequest.get(url)
                    .header("PRIVATE-TOKEN", gitlabToken)
                    .timeout(15000)
                    .execute();
            if (response.isOk()) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.isArray()) {
                    for (JsonNode n : root) {
                        commits.add(n);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取分支提交异常", e);
        }
        return commits;
    }

    private JsonNode getCommitDetail(Integer projectId, String commitSha) {
        try {
            String baseUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
            String url = baseUrl + "/api/v4/projects/" + projectId + "/repository/commits/" + commitSha;
            HttpResponse response = HttpRequest.get(url)
                    .header("PRIVATE-TOKEN", gitlabToken)
                    .timeout(15000)
                    .execute();
            if (response.isOk()) {
                return objectMapper.readTree(response.body());
            }
        } catch (Exception e) {
            log.error("获取 Commit 详情异常", e);
        }
        return null;
    }
}
