# ============================================================
# 前端管理后台 Dockerfile - 多阶段构建
# 基础镜像: node:20-alpine / nginx:stable-alpine (支持 ARM + X86)
# ============================================================

# --- Stage 1: Build ---
FROM node:20-alpine AS builder

WORKDIR /build

# 先复制依赖文件利用缓存
COPY package.json package-lock.json* ./

# 安装依赖
RUN npm install --registry=https://registry.npmmirror.com

# 复制源码并构建
COPY . .
RUN npm run build

# --- Stage 2: Runtime ---
FROM nginx:stable-alpine

LABEL maintainer="windpower-diag"
LABEL description="风力发电机故障诊断系统 - 管理后台前端"

# 移除默认配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制产物
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
