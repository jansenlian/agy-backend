package com.greatmap.agy.util.service.gitlab.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatmap.agy.util.api.dto.gitlab.GitLabBranchDTO;
import com.greatmap.agy.util.service.gitlab.entity.GitLabBranch;
import com.greatmap.agy.util.service.gitlab.entity.GitLabProject;
import com.greatmap.agy.util.service.gitlab.mapper.GitLabBranchMapper;
import com.greatmap.agy.util.service.gitlab.service.GitLabBranchService;
import com.greatmap.agy.util.service.gitlab.service.GitLabCommitService;
import com.greatmap.agy.util.service.gitlab.service.GitLabProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GitLab 分支持久层服务实现
 *
 * @author AGY
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabBranchServiceImpl extends ServiceImpl<GitLabBranchMapper, GitLabBranch>
        implements GitLabBranchService {

    private final ObjectMapper objectMapper;
    private final GitLabCommitService gitLabCommitService;
    private final GitLabProjectService gitLabProjectService;

    @Value("${gitlab.url:https://gitlab.example.com/}")
    private String gitlabUrl;

    @Value("${gitlab.token:}")
    private String gitlabToken;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncActiveProjectBranches() {
        List<GitLabProject> activeProjects = gitLabProjectService.list(new LambdaQueryWrapper<GitLabProject>()
                .eq(GitLabProject::getDelFlag, 0));

        int totalBranches = 0;
        int totalCommits = 0;

        for (GitLabProject project : activeProjects) {
            try {
                List<JsonNode> branches = getProjectBranches(project.getProjectId());
                String defaultBranch = project.getDefaultBranch();

                for (JsonNode branch : branches) {
                    GitLabBranch entity = convertToBranchEntity(project.getProjectId(), branch, defaultBranch);
                    GitLabBranch existing = getOne(new LambdaQueryWrapper<GitLabBranch>()
                            .eq(GitLabBranch::getProjectId, entity.getProjectId())
                            .eq(GitLabBranch::getName, entity.getName())
                            .last("LIMIT 1"));
                    if (existing != null) {
                        entity.setId(existing.getId());
                        updateById(entity);
                    } else {
                        save(entity);
                    }
                    totalBranches++;

                    // 同步分支的提交记录
                    int commitsCount = gitLabCommitService.syncCommits(entity.getId(), project.getProjectId());
                    totalCommits += commitsCount;
                }
            } catch (Exception e) {
                log.error("同步项目 {} 的分支失败", project.getName(), e);
            }
        }

        log.info("GitLab 分支同步完成，共处理 {} 个分支，{} 条提交记录", totalBranches, totalCommits);
        return totalBranches;
    }

    @Override
    public List<GitLabBranchDTO> listAllBranches() {
        List<GitLabBranch> branches = list(new LambdaQueryWrapper<GitLabBranch>()
                .eq(GitLabBranch::getDelFlag, 0)
                .orderByDesc(GitLabBranch::getCreateTime));

        List<GitLabBranchDTO> result = new ArrayList<>();
        for (GitLabBranch branch : branches) {
            result.add(convertEntityToDTO(branch));
        }
        return result;
    }

    @Override
    public List<GitLabBranchDTO> getBranchesByProjectId(Integer projectId) {
        List<GitLabBranch> branches = list(new LambdaQueryWrapper<GitLabBranch>()
                .eq(GitLabBranch::getProjectId, projectId)
                .eq(GitLabBranch::getDelFlag, 0));

        List<GitLabBranchDTO> result = new ArrayList<>();
        for (GitLabBranch branch : branches) {
            result.add(convertEntityToDTO(branch));
        }
        return result;
    }

    private List<JsonNode> getProjectBranches(Integer projectId) {
        if (StringUtils.isBlank(gitlabToken)) {
            return Collections.emptyList();
        }

        List<JsonNode> allBranches = new ArrayList<>();
        int page = 1;
        int perPage = 100;

        try {
            while (true) {
                String baseUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
                String url = baseUrl + "/api/v4/projects/" + projectId + "/repository/branches?per_page=" + perPage + "&page=" + page;

                HttpResponse response = HttpRequest.get(url)
                        .header("PRIVATE-TOKEN", gitlabToken)
                        .timeout(15000)
                        .execute();

                if (response.isOk()) {
                    JsonNode rootNode = objectMapper.readTree(response.body());
                    if (rootNode.isArray() && !rootNode.isEmpty()) {
                        for (JsonNode node : rootNode) {
                            allBranches.add(node);
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
            log.error("获取项目 {} 的分支列表异常", projectId, e);
        }
        return allBranches;
    }

    private GitLabBranch convertToBranchEntity(Integer projectId, JsonNode branch, String defaultBranch) {
        GitLabBranch entity = new GitLabBranch();
        entity.setProjectId(projectId);
        entity.setName(branch.path("name").asText());
        entity.setIsDefault(StringUtils.equals(entity.getName(), defaultBranch) ? 1 : 0);
        entity.setWebUrl(branch.path("web_url").asText(null));

        JsonNode commit = branch.path("commit");
        entity.setCommitId(commit.path("id").asText(null));
        entity.setCommittedDate(parseDateTime(commit.path("committed_date").asText(null)));
        return entity;
    }

    private GitLabBranchDTO convertEntityToDTO(GitLabBranch entity) {
        GitLabBranchDTO dto = new GitLabBranchDTO();
        dto.setProjectId(entity.getProjectId());
        dto.setName(entity.getName());
        dto.setIsDefault(entity.getIsDefault());
        dto.setWebUrl(entity.getWebUrl());
        dto.setCommitId(entity.getCommitId());
        if (entity.getCommittedDate() != null) {
            dto.setCommittedDate(entity.getCommittedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (entity.getCreateTime() != null) {
            dto.setCreateTime(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(dateStr).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
}
