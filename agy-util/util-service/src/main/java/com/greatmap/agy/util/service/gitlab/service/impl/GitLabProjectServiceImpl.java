package com.greatmap.agy.util.service.gitlab.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greatmap.agy.util.api.dto.gitlab.GitLabProjectDTO;
import com.greatmap.agy.util.service.gitlab.entity.GitLabProject;
import com.greatmap.agy.util.service.gitlab.mapper.GitLabProjectMapper;
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
 * GitLab 项目持久层服务实现
 *
 * @author AGY
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabProjectServiceImpl extends ServiceImpl<GitLabProjectMapper, GitLabProject>
        implements GitLabProjectService {

    private final ObjectMapper objectMapper;

    @Value("${gitlab.url:http://gitlab.greatmap.com.cn:10000/}")
    private String gitlabUrl;

    @Value("${gitlab.token:}")
    private String gitlabToken;

    @Override
    public List<GitLabProjectDTO> getAllProjects() {
        List<GitLabProjectDTO> result = new ArrayList<>();
        List<JsonNode> projects = fetchAllProjects();
        for (JsonNode project : projects) {
            result.add(convertToDTO(project));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncAllProjects() {
        List<JsonNode> projects = fetchAllProjects();
        List<GitLabProject> projectEntities = new ArrayList<>();

        for (JsonNode project : projects) {
            GitLabProject entity = convertToEntity(project);
            // 检查数据库是否存在
            GitLabProject existing = getOne(new LambdaQueryWrapper<GitLabProject>()
                    .eq(GitLabProject::getProjectId, entity.getProjectId())
                    .last("LIMIT 1"));
            if (existing != null) {
                entity.setId(existing.getId());
                updateById(entity);
            } else {
                save(entity);
            }
            projectEntities.add(entity);
        }

        log.info("GitLab 项目同步完成，共处理 {} 个项目", projectEntities.size());
        return projectEntities.size();
    }

    @Override
    public GitLabProjectDTO getProjectById(Integer projectId) {
        GitLabProject project = getOne(new LambdaQueryWrapper<GitLabProject>()
                .eq(GitLabProject::getProjectId, projectId)
                .last("LIMIT 1"));
        return project != null ? convertEntityToDTO(project) : null;
    }

    @Override
    public List<GitLabProjectDTO> listAllProjects() {
        List<GitLabProject> projects = list(new LambdaQueryWrapper<GitLabProject>()
                .eq(GitLabProject::getDelFlag, 0)
                .orderByDesc(GitLabProject::getCreateTime));

        List<GitLabProjectDTO> result = new ArrayList<>();
        for (GitLabProject project : projects) {
            result.add(convertEntityToDTO(project));
        }
        return result;
    }

    private List<JsonNode> fetchAllProjects() {
        if (StringUtils.isBlank(gitlabToken)) {
            log.warn("GitLab Token 未配置，无法请求远程项目列表");
            return Collections.emptyList();
        }

        List<JsonNode> allProjects = new ArrayList<>();
        int page = 1;
        int perPage = 100;

        try {
            while (true) {
                String baseUrl = gitlabUrl.endsWith("/") ? gitlabUrl.substring(0, gitlabUrl.length() - 1) : gitlabUrl;
                String url = baseUrl + "/api/v4/projects?membership=true&per_page=" + perPage + "&page=" + page;

                HttpResponse response = HttpRequest.get(url)
                        .header("PRIVATE-TOKEN", gitlabToken)
                        .timeout(15000)
                        .execute();

                if (response.isOk()) {
                    JsonNode rootNode = objectMapper.readTree(response.body());
                    if (rootNode.isArray() && !rootNode.isEmpty()) {
                        for (JsonNode node : rootNode) {
                            allProjects.add(node);
                        }
                        page++;
                    } else {
                        break;
                    }
                } else {
                    log.error("获取 GitLab 项目列表失败：HTTP 状态码 {}", response.getStatus());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("获取 GitLab 项目列表异常", e);
        }
        return allProjects;
    }

    private GitLabProject convertToEntity(JsonNode node) {
        GitLabProject entity = new GitLabProject();
        entity.setProjectId(node.path("id").asInt());
        entity.setDescription(node.path("description").asText(null));
        entity.setName(node.path("name").asText());
        entity.setPathWithNamespace(node.path("path_with_namespace").asText());
        entity.setDefaultBranch(node.path("default_branch").asText(null));
        entity.setCreatedAt(parseDateTime(node.path("created_at").asText(null)));
        entity.setHttpUrlToRepo(node.path("http_url_to_repo").asText(null));
        entity.setLastActivityAt(parseDateTime(node.path("last_activity_at").asText(null)));
        entity.setIsActive(0);
        return entity;
    }

    private GitLabProjectDTO convertToDTO(JsonNode node) {
        GitLabProjectDTO dto = new GitLabProjectDTO();
        dto.setProjectId(node.path("id").asInt());
        dto.setDescription(node.path("description").asText(null));
        dto.setName(node.path("name").asText());
        dto.setPathWithNamespace(node.path("path_with_namespace").asText());
        dto.setDefaultBranch(node.path("default_branch").asText(null));
        dto.setCreatedAt(node.path("created_at").asText(null));
        dto.setHttpUrlToRepo(node.path("http_url_to_repo").asText(null));
        dto.setLastActivityAt(node.path("last_activity_at").asText(null));
        dto.setIsActive(0);
        return dto;
    }

    private GitLabProjectDTO convertEntityToDTO(GitLabProject entity) {
        GitLabProjectDTO dto = new GitLabProjectDTO();
        dto.setProjectId(entity.getProjectId());
        dto.setDescription(entity.getDescription());
        dto.setName(entity.getName());
        dto.setPathWithNamespace(entity.getPathWithNamespace());
        dto.setDefaultBranch(entity.getDefaultBranch());
        dto.setHttpUrlToRepo(entity.getHttpUrlToRepo());
        dto.setIsActive(entity.getIsActive());
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (entity.getLastActivityAt() != null) {
            dto.setLastActivityAt(entity.getLastActivityAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
