package com.greatmap.agy.util.service.gitlab.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatmap.agy.util.api.dto.gitlab.GitLabCommitDTO;
import com.greatmap.agy.util.service.gitlab.entity.GitLabCommit;
import com.greatmap.agy.util.service.gitlab.mapper.GitLabCommitMapper;
import com.greatmap.agy.util.service.gitlab.service.GitLabCommitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GitLab 提交记录持久层服务实现
 *
 * @author AGY
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabCommitServiceImpl extends ServiceImpl<GitLabCommitMapper, GitLabCommit>
        implements GitLabCommitService {

    private final ObjectMapper objectMapper;

    @Value("${gitlab.url:http://gitlab.greatmap.com.cn:10000/}")
    private String gitlabUrl;

    @Value("${gitlab.token:}")
    private String gitlabToken;

    @Override
    public int syncCommits(String branchId, Integer projectId) {
        List<JsonNode> commits = getBranchCommits(projectId, branchId);
        int count = 0;

        for (JsonNode commit : commits) {
            try {
                GitLabCommit entity = convertToCommitEntity(branchId, projectId, commit);
                GitLabCommit existing = getOne(new LambdaQueryWrapper<GitLabCommit>()
                        .eq(GitLabCommit::getProjectId, projectId)
                        .eq(GitLabCommit::getCommitId, entity.getCommitId())
                        .last("LIMIT 1"));
                if (existing != null) {
                    entity.setId(existing.getId());
                    updateById(entity);
                } else {
                    save(entity);
                }
                count++;
            } catch (Exception e) {
                log.error("保存提交 {} 失败", commit.path("id").asText(), e);
            }
        }
        return count;
    }

    @Override
    public List<GitLabCommitDTO> getCommitsByBranchId(String branchId) {
        List<GitLabCommit> commits = list(new LambdaQueryWrapper<GitLabCommit>()
                .eq(GitLabCommit::getBranchId, branchId)
                .eq(GitLabCommit::getDelFlag, 0)
                .orderByDesc(GitLabCommit::getCommittedDate));

        List<GitLabCommitDTO> result = new ArrayList<>();
        for (GitLabCommit commit : commits) {
            result.add(convertEntityToDTO(commit));
        }
        return result;
    }

    private List<JsonNode> getBranchCommits(Integer projectId, String branchName) {
        if (StringUtils.isBlank(gitlabToken)) {
            return Collections.emptyList();
        }

        List<JsonNode> allCommits = new ArrayList<>();
        try {
            String baseUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
            String encodedBranch = URLEncoder.encode(branchName, StandardCharsets.UTF_8);
            String url = baseUrl + "/api/v4/projects/" + projectId + "/repository/commits?ref_name=" + encodedBranch + "&per_page=100";

            HttpResponse response = HttpRequest.get(url)
                    .header("PRIVATE-TOKEN", gitlabToken)
                    .timeout(15000)
                    .execute();

            if (response.isOk()) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                if (rootNode.isArray()) {
                    for (JsonNode node : rootNode) {
                        allCommits.add(node);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取项目 {} 分支 {} 提交记录异常", projectId, branchName, e);
        }
        return allCommits;
    }

    private GitLabCommit convertToCommitEntity(String branchId, Integer projectId, JsonNode commit) {
        GitLabCommit entity = new GitLabCommit();
        entity.setBranchId(branchId);
        entity.setProjectId(projectId);
        entity.setCommitId(commit.path("id").asText());
        entity.setTitle(commit.path("title").asText(null));
        entity.setMessage(commit.path("message").asText(null));
        entity.setCommitterName(commit.path("committer_name").asText(null));
        entity.setCommitterEmail(commit.path("committer_email").asText(null));
        entity.setCommittedDate(parseDateTime(commit.path("committed_date").asText(null)));
        return entity;
    }

    private GitLabCommitDTO convertEntityToDTO(GitLabCommit entity) {
        GitLabCommitDTO dto = new GitLabCommitDTO();
        dto.setProjectId(entity.getProjectId());
        dto.setBranchId(entity.getBranchId());
        dto.setCommitId(entity.getCommitId());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setCommitterName(entity.getCommitterName());
        dto.setCommitterEmail(entity.getCommitterEmail());
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
