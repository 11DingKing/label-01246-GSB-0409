# ============================================================
# 后端 Dockerfile - 多阶段构建
# 基础镜像: eclipse-temurin (支持 linux/amd64 + linux/arm64)
# ============================================================

# --- Stage 1: Build ---
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# 安装 Maven
ARG MAVEN_VERSION=3.9.9
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar -xz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 先复制 pom.xml 利用 Docker 缓存下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并编译
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre

LABEL maintainer="windpower-diag"
LABEL description="风力发电机故障诊断系统 - 后端服务"

# 安装 curl 用于健康检查
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 从构建阶段复制 jar
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8082

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=60s \
  CMD curl -sf http://localhost:8082/ || exit 1

# 启动命令 - Docker 环境使用 --env=docker 加载 app-docker.yml 覆盖数据库连接
ENTRYPOINT ["java", "-jar", "app.jar", "--env=docker"]
