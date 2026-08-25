package com.greatmap.agy.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * 带有 @Tool 方法的工具基类
 */
public abstract class BaseCustomTool {

    @Tool("从基类继承的系统健康诊断工具")
    public String baseHealthCheck(@P("服务标识") String serviceName) {
        return "【基类工具探测】服务 [" + serviceName + "] 运行正常";
    }
}
