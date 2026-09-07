package com.greatmap.agy.web;

import com.greatmap.agy.common.annotation.Anonymous;
import com.greatmap.agy.common.annotation.PermitAll;
import com.greatmap.agy.common.domain.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FrameworkArchitecture 平台基座分层与基础契约测试")
class FrameworkArchitectureTest {

    @Test
    @DisplayName("测试基础统一响应状态码契约")
    void testStandardResultContract() {
        Result<String> res = Result.ok("Framework Platform Online");
        assertNotNull(res);
        assertEquals(200, res.getCode());
        assertEquals("Framework Platform Online", res.getData());
        assertTrue(res.getTimestamp() > 0);
    }

    @Test
    @DisplayName("架构安全强约束卡点：所有 Controller 接口方法必须显式声明安全意图 (@PreAuthorize / @Anonymous / @PermitAll)")
    void checkAllControllerMethodsHaveExplicitSecurityDeclaration() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        Resource[] resources = resolver.getResources("classpath*:com/greatmap/agy/**/controller/**/*.class");

        List<String> violations = new ArrayList<>();
        int scannedCount = 0;

        for (Resource resource : resources) {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            String className = metadataReader.getClassMetadata().getClassName();
            Class<?> clazz = Class.forName(className);

            // 仅对标注了 @RestController 或 @Controller 的类进行强安全审计
            if (!clazz.isAnnotationPresent(RestController.class) && !clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            boolean classLevelSecurity = clazz.isAnnotationPresent(PreAuthorize.class)
                    || clazz.isAnnotationPresent(Anonymous.class)
                    || clazz.isAnnotationPresent(PermitAll.class)
                    || clazz.isAnnotationPresent(jakarta.annotation.security.PermitAll.class);

            for (Method method : clazz.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                // 检查是否为 Web 请求映射接口
                boolean isMappingMethod = AnnotatedElementUtils.isAnnotated(method, RequestMapping.class)
                        || AnnotatedElementUtils.isAnnotated(method, GetMapping.class)
                        || AnnotatedElementUtils.isAnnotated(method, PostMapping.class)
                        || AnnotatedElementUtils.isAnnotated(method, PutMapping.class)
                        || AnnotatedElementUtils.isAnnotated(method, DeleteMapping.class)
                        || AnnotatedElementUtils.isAnnotated(method, PatchMapping.class);

                if (!isMappingMethod) {
                    continue;
                }

                scannedCount++;

                boolean hasSecurityAnnotation = classLevelSecurity
                        || method.isAnnotationPresent(PreAuthorize.class)
                        || method.isAnnotationPresent(Anonymous.class)
                        || method.isAnnotationPresent(PermitAll.class)
                        || method.isAnnotationPresent(jakarta.annotation.security.PermitAll.class);

                if (!hasSecurityAnnotation) {
                    violations.add(String.format("❌ 发现未声明安全意图的裸接口 (Fail-Open 风险): %s#%s()，必须显式标注 @PreAuthorize(...)、@PreAuthorize(\"isAuthenticated()\")、@Anonymous 或 @PermitAll",
                            clazz.getSimpleName(), method.getName()));
                }
            }
        }

        assertTrue(scannedCount > 0, "扫描到的 Controller 接口数量应大于 0");
        assertTrue(violations.isEmpty(), "架构合规检查未通过，发现裸接口遗漏安全声明：\n" + String.join("\n", violations));
    }
}
