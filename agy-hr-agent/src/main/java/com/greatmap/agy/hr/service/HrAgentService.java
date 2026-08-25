package com.greatmap.agy.hr.service;

/**
 * 智能人事与运维资产业务服务契约
 */
public interface HrAgentService {

    /**
     * 查询员工 360° 全景档案
     *
     * @param nameOrId 员工姓名或工号
     * @return 员工全景档案 JSON 字符串或未查到提示
     */
    String queryEmployeeProfile(String nameOrId);

    /**
     * 查询员工历史薪酬与人力成本明细
     *
     * @param nameOrId 员工姓名或工号
     * @return 薪酬成本记录 JSON 字符串或未查到提示
     */
    String queryEmployeeSalaryHistory(String nameOrId);

    /**
     * 语义检索员工知识库
     *
     * @param keyword 关键词或自然语言特征描述
     * @return 知识库检索匹配分块结果
     */
    String searchEmployeeKnowledge(String keyword);

    /**
     * 查询测试环境服务器资产配置
     *
     * @param projectNameOrIp 项目名称或 IP 地址
     * @return 服务器环境资产 JSON 字符串或未查到提示
     */
    String queryServerEnvironment(String projectNameOrIp);
}
