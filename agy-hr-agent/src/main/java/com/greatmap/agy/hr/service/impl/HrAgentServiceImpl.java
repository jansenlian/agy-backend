package com.greatmap.agy.hr.service.impl;

import cn.hutool.json.JSONUtil;
import com.greatmap.agy.hr.entity.EmployeeKnowledgeVector;
import com.greatmap.agy.hr.entity.EmployeeProfile;
import com.greatmap.agy.hr.entity.EmployeeSalaryHistory;
import com.greatmap.agy.hr.entity.ServerEnvironment;
import com.greatmap.agy.hr.mapper.EmployeeKnowledgeVectorMapper;
import com.greatmap.agy.hr.mapper.EmployeeProfileMapper;
import com.greatmap.agy.hr.mapper.EmployeeSalaryHistoryMapper;
import com.greatmap.agy.hr.mapper.ServerEnvironmentMapper;
import com.greatmap.agy.hr.service.HrAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能人事与运维资产业务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrAgentServiceImpl implements HrAgentService {

    private final EmployeeProfileMapper employeeProfileMapper;
    private final EmployeeSalaryHistoryMapper employeeSalaryHistoryMapper;
    private final ServerEnvironmentMapper serverEnvironmentMapper;
    private final EmployeeKnowledgeVectorMapper employeeKnowledgeVectorMapper;

    @Override
    public String queryEmployeeProfile(String nameOrId) {
        if (nameOrId == null || nameOrId.trim().isEmpty()) {
            return "【查询结果】员工姓名或工号不能为空";
        }
        log.info(">>> [HrAgentService] 执行员工档案查询: {}", nameOrId);
        List<EmployeeProfile> list = employeeProfileMapper.findByNameOrId(nameOrId.trim());
        if (list.isEmpty()) {
            list = employeeProfileMapper.fuzzySearch(nameOrId.trim());
        }
        if (list.isEmpty()) {
            return String.format("【查询结果】未在员工档案库中检索到姓名为 [%s] 的员工记录", nameOrId);
        }
        return JSONUtil.toJsonPrettyStr(list);
    }

    @Override
    public String queryEmployeeSalaryHistory(String nameOrId) {
        if (nameOrId == null || nameOrId.trim().isEmpty()) {
            return "【查询结果】员工姓名或工号不能为空";
        }
        log.info(">>> [HrAgentService] 执行员工薪酬成本查询: {}", nameOrId);
        List<EmployeeSalaryHistory> list = employeeSalaryHistoryMapper.findRecentSalaries(nameOrId.trim());
        if (list.isEmpty()) {
            return String.format("【查询结果】未查询到员工 [%s] 的历史薪酬成本记录", nameOrId);
        }
        return JSONUtil.toJsonPrettyStr(list);
    }

    @Override
    public String searchEmployeeKnowledge(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "【查询结果】检索关键词不能为空";
        }
        log.info(">>> [HrAgentService] 执行员工知识库语义检索: {}", keyword);
        List<EmployeeKnowledgeVector> list = employeeKnowledgeVectorMapper.searchByKeyword(keyword.trim());
        if (list.isEmpty()) {
            return String.format("【查询结果】知识库中暂未匹配到包含关键词 [%s] 的员工经历", keyword);
        }
        StringBuilder sb = new StringBuilder("【知识库检索结果】\n");
        for (EmployeeKnowledgeVector item : list) {
            sb.append("- ").append(item.getKnowledgeChunk()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String queryServerEnvironment(String projectNameOrIp) {
        if (projectNameOrIp == null || projectNameOrIp.trim().isEmpty()) {
            return "【查询结果】项目名称或 IP 地址不能为空";
        }
        log.info(">>> [HrAgentService] 执行测试环境服务器资产查询: {}", projectNameOrIp);
        List<ServerEnvironment> list = serverEnvironmentMapper.findByProjectOrIp(projectNameOrIp.trim());
        if (list.isEmpty()) {
            return String.format("【查询结果】未查询到与 [%s] 相关的测试环境服务器资产信息", projectNameOrIp);
        }
        return JSONUtil.toJsonPrettyStr(list);
    }
}
