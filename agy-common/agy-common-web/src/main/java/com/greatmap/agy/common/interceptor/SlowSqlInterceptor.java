package com.greatmap.agy.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.Properties;

/**
 * 生产级慢 SQL 监控拦截器 (自动统计单条 SQL 物理耗时并告警)
 * 纯日志监控，零侵入、零入库、快路径纳秒级运算 (<0.01% 开销)
 */
@Slf4j
@Component
@ConditionalOnClass(StatementHandler.class)
@Intercepts({
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, org.apache.ibatis.session.ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
    @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})
public class SlowSqlInterceptor implements Interceptor {

    /**
     * 慢 SQL 触发告警阈值 (毫秒)
     */
    private static final long SLOW_SQL_THRESHOLD_MS = 500L;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            if (costTime >= SLOW_SQL_THRESHOLD_MS) {
                try {
                    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
                    String boundSql = statementHandler.getBoundSql().getSql();
                    // 压缩换行与多余连续空格
                    String cleanSql = boundSql.replaceAll("\\s+", " ").trim();
                    log.warn("🐢 慢SQL告警: 处理耗时过长, 耗时: {}ms (阈值: {}ms), 完整SQL: [{}]",
                            costTime, SLOW_SQL_THRESHOLD_MS, cleanSql);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
