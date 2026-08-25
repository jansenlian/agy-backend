package com.greatmap.agy.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 全局通用配置
 * 1. 自动装配 SqlSessionFactory 并绑定 MetaObjectHandler (审计字段自动填充)
 * 2. 注入 PaginationInnerInterceptor (PostgreSQL 分页插件)
 * 3. 注入 BlockAttackInnerInterceptor (防全表更新与物理/逻辑全表删除安全插件)
 * 4. 注入 OptimisticLockerInnerInterceptor (乐观锁支持)
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(name = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")
@ConditionalOnClass({SqlSessionFactory.class, MybatisSqlSessionFactoryBean.class})
public class MyBatisPlusConfig {

    /**
     * 显式装配 SqlSessionFactory，并将 MetaObjectHandler (自动填充处理器) 与 插件链注册进 GlobalConfig
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(SqlSessionFactory.class)
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource,
                                               MybatisPlusInterceptor interceptor,
                                               ObjectProvider<MetaObjectHandler> metaObjectHandlerProvider) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPlugins(interceptor);

        com.baomidou.mybatisplus.core.MybatisConfiguration configuration = new com.baomidou.mybatisplus.core.MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
        factoryBean.setConfiguration(configuration);

        // 自动加载 mapper XML 映射文件
        org.springframework.core.io.support.PathMatchingResourcePatternResolver resolver = new org.springframework.core.io.support.PathMatchingResourcePatternResolver();
        try {
            org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:mapper/**/*.xml");
            if (resources != null && resources.length > 0) {
                factoryBean.setMapperLocations(resources);
            }
        } catch (Exception ignored) {
        }

        // 将自动填充处理器与逻辑删除注册进 MyBatis-Plus 的 GlobalConfig，确保全局 100% 自动注入审计与时间字段
        GlobalConfig globalConfig = new GlobalConfig();
        metaObjectHandlerProvider.ifAvailable(globalConfig::setMetaObjectHandler);
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setLogicDeleteField("delFlag");
        dbConfig.setLogicDeleteValue("1");
        dbConfig.setLogicNotDeleteValue("0");
        globalConfig.setDbConfig(dbConfig);
        factoryBean.setGlobalConfig(globalConfig);

        return factoryBean.getObject();
    }

    /**
     * 装配 SqlSessionTemplate 模板
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(org.mybatis.spring.SqlSessionTemplate.class)
    public org.mybatis.spring.SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new org.mybatis.spring.SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * MyBatis-Plus 综合拦截器链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 1. 防全表更新与全表删除插件 (阻断无 WHERE 条件的 update/delete 操作)
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 3. PostgreSQL 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}
