# AGY 研发效能与安全工具微服务 (agy-util)

基于 Java 25 + Spring Boot 4.1.0 架构的研发效能与安全加解密微服务工程。

---

## 📂 模块架构

- 📦 **`util-api`**: 对外服务契约声明包 (DTO, VO, 校验注解) —— 物理隔离，无 Entity/Mapper。
- ⚙️ **`util-service`**: 业务持久层 (Entity, Mapper, ServiceImpl, 集成 `geo-all` 空间数据包与 PostgreSQL)。
- 🚀 **`util-web`**: 控制器与微服务启动入口 (`AgyUtilApplication`，运行端口: 9202)。

---

## 🛠️ 核心功能

1. 🔐 **密码学加解密与验签服务 (`/api/v1/crypto`)**:
   - **国密 SM2 / RSA 非对称加解密**: 密钥对生成、公钥加密、私钥解密、签名与验签；
   - **国密 SM4 / AES 对称加解密**: CBC / GCM 高性能块加密；
   - **摘要哈希算法**: SM3、SHA-256、MD5 消息摘要计算。
2. 📐 **CAD 空间数据与拓扑能力**:
   - 引入 `com.greatmap.agy:geo-all`，支持本地 DWG/DXF 图纸直接解析与 GeoJSON 矢量提取。

---

## 📖 全局架构规范

- 平台完整前后端架构规格书详见平台基座：[`agy-platform/docs/`](../agy-platform/docs/)
