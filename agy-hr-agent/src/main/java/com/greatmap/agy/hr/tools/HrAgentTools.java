package com.greatmap.agy.hr.tools;

import com.greatmap.agy.hr.service.HrAgentService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 企业级智能人事、薪酬与运维资产查询业务工具集
 * 自动由 agy-agent-common 底座扫描并注册到大模型大脑中
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HrAgentTools {

    private final HrAgentService hrAgentService;

    @Tool("根据员工姓名或工号，精确查询员工 360° 全景档案（包括工号、姓名、部门、职位、入职时间、毕业院校、专业、学历、职称、产品组、参与项目列表、现住址）")
    public String queryEmployeeProfile(
            @P("员工姓名或工号，如：王健、周绍涛、184、1") String nameOrId) {
        log.info(">>> [Agent Tool 触发] 查询员工档案: {}", nameOrId);
        return hrAgentService.queryEmployeeProfile(nameOrId);
    }

    @Tool("根据员工姓名或工号，查询该员工历月月度真实薪酬发放与实际人力成本记录明细（来源于真实薪酬核算表 aa.xlsx）")
    public String queryEmployeeSalaryHistory(
            @P("员工姓名或工号，如：王健、周绍涛、王阳、401") String nameOrId) {
        log.info(">>> [Agent Tool 触发] 查询员工薪酬成本历史: {}", nameOrId);
        return hrAgentService.queryEmployeeSalaryHistory(nameOrId);
    }

    @Tool("根据自然语言特征（如技能、特定项目、居住地、学历或工作经历关键词）在员工全景知识库中进行智能模糊检索匹配")
    public String searchEmployeeKnowledge(
            @P("搜索关键词或特征描述，如：一码管地、机电一体化、河南师范大学、地理信息系统工程师") String keyword) {
        log.info(">>> [Agent Tool 触发] 语义检索员工知识库: {}", keyword);
        return hrAgentService.searchEmployeeKnowledge(keyword);
    }

    @Tool("根据项目名称或 IP 地址，查询该项目在测试环境的服务器配置（包括操作系统、IP、登录账号、密码、Swagger 接口文档地址、磁盘信息）")
    public String queryServerEnvironment(
            @P("项目名称或 IP 地址，如：示例项目、192.168.1.100") String projectNameOrIp) {
        log.info(">>> [Agent Tool 触发] 查询测试环境服务器: {}", projectNameOrIp);
        return hrAgentService.queryServerEnvironment(projectNameOrIp);
    }
}
