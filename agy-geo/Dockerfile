# 基础镜像: OpenJDK 25 slim
FROM openjdk:25-jdk-slim

# 设置时区与安装基础依赖以及 LibreDWG 工具
ENV TZ=Asia/Shanghai
RUN apt-get update && apt-get install -y --no-install-recommends \
    tzdata \
    libredwg-tools \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制打包产物
COPY target/dwg-tool-*.jar app.jar

# 暴露端口
EXPOSE 8088

# 启动微服务
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
