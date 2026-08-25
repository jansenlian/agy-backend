# AGY 依赖版本总控 BOM (agy-dependencies)

这是 AGY 极速基座体系下的**全域依赖版本总控 BOM (Bill of Materials)**。

---

## 🎯 核心使命

1. **统一全平台版本**：集中管理 Spring Boot、MyBatis-Plus、PostgreSQL、Hutool、EasyExcel、LangChain4j、JTS 等所有技术栈组件的版本；
2. **根治版本漂移**：业务工程与 Agent 只需要引入本 BOM，声明依赖时**一律不需要写 `<version>` 标签**；
3. **一键全域升级**：后续升级任何组件版本，只需在本工程修改并发布，所有微服务自动同步生效。

---

## 🛠️ 业务微服务接入规范

在各工程根 `pom.xml` 的 `<dependencyManagement>` 中以 `<scope>import</scope>` 引入：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.greatmap.agy</groupId>
            <artifactId>agy-dependencies</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
