# agy-aks-agent

AGY AKS阿克苏智慧耕地违法违规预警诊断与驾驶舱大盘宏观统计智能体微服务(基于`agy-agent-common`底座与Java 25+Spring Boot 4.1.0构建).

## 1. 项目简介

`agy-aks-agent`是面向阿克苏地区智慧耕地与国土资源监管的专业AI智能体,直连PostgreSQL业务主库(`aks`),提供以下两大核心能力:
- **违法违规与风险预警深度诊断(`YjAgentTools`)**: 支持全区预警多维检索、穿透合同与地块现状进行违规因果诊断(如耕地非粮化、非法占用、租金欠缴、到期未复耕等),自动输出整改处置建议.
- **领导驾驶舱与大盘宏观统计(`FarmlandStatAgentTools`)**: 实时聚合阿克苏全区及9个县市区的合同总数、承包总面积、粮菜种植结构分布、非粮化比例、租金收缴进度与审批流转合规状态.

## 2. 技术架构

- 运行环境: JDK 25
- 核心框架: Spring Boot 4.1.0
- AI底座: LangChain4j 0.36.2 + agy-agent-common
- 多数据源: Baomidou Dynamic DataSource 4.3.1 (master业务主库 + sde空间库)
- 持久层框架: MyBatis-Plus 3.5.9 + PostgreSQL 42.7.4 + HikariCP
- 缓存与工具: Redis + Lombok 1.18.46 + Hutool 5.8.34

## 3. 核心工具集(Agent Toolsets)

### 3.1 违法违规与风险预警诊断工具集(`YjAgentTools`)
- `queryWarningList`: 分页与多条件筛选全区预警台账(支持按区县、类型、起止时间与关键词).
- `diagnoseWarningCase`: 穿透诊断特定预警单/合同/地块违规成因,联动关联合同台账与地块现状,输出AI诊断分析与处置建议.
- `getWarningStatistics`: 统计全区及各区县预警周期分布、高发类型与风险排行.

### 3.2 领导驾驶舱与大盘宏观统计工具集(`FarmlandStatAgentTools`)
- `getDistrictMacroOverview`: 汇总阿克苏各区县合同总数、承包总面积、地块总数与租金总额.
- `getPlantingAndCropStats`: 统计粮食作物(小麦/玉米)、经济作物(棉花/林果)种植面积与非粮化比例.
- `getRentAndFinancialStats`: 汇总各区县租金总额、亩均租金指导价分布与收缴进度.
- `getAuditStatusStats`: 统计合同与地块审批流转状态与合规履约情况.

## 4. 核心接口

| 接口类型 | 请求方式 | 请求路径 | 描述 |
| :--- | :--- | :--- | :--- |
| 同步智能问答 | POST | `/api/agent/chat` | 接收JSON参数进行同步问答与Tool自动调用 |
| SSE流式打字机 | GET | `/api/agent/chat/stream` | 接收Query参数以SSE流式实时输出结果 |

## 5. 本地启动

### 5.1 编译工程
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home mvn clean compile
```

### 5.2 启动服务
运行`com.greatmap.agy.aks.AgyAksAgentApplication`主类即可.
默认服务端口:`8092`.
