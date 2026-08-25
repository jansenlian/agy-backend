# AGY 业务微服务脚手架模板工程 (agy-business-demo)

这是 AGY 极速基座体系下的**标准业务后端微服务脚手架模板工程**（`agy-business-demo`）。后续创建新的业务微服务时，**直接复制本工程即可快速衍生新的独立业务系统**！

---

## 📂 工程模块结构

- 📜 **`business-api`**：对外服务契约声明包 (DTO、VO、Client、Validation) —— 物理隔离绝不含 Entity/Mapper
- 🗄️ **`business-service`**：业务持久层 (Entity、Mapper、ServiceImpl、PostgreSQL + MyBatis-Plus)
- 🚀 **`business-web`**：（默认端口 9201）业务运行入口、Controller、Swagger 接口文档与安全切面

---

## 🛠️ 快速衍生新业务微服务

1. **复制工程**：将 `agy-business-demo` 整体复制为新项目目录（如 `agy-order`、`agy-inspection`）。
2. **全局替换 ArtifactId 与包名**：在根 `pom.xml` 及各子模块中批量将 `business` 替换为新业务代号（如 `com.greatmap.agy.order`）。
3. **日常启动**：直接运行 `business-web` 模块中的 `AgyBusinessApplication.java` 即可。

---

## 💻 前端门户说明

所有业务系统的前端界面统一在 **[`agy-front`](../agy-front/)** 独立大前端门户中集中维护与运行。
