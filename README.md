# AI零代码应用生成平台

**AI零代码应用生成平台**基于LangChain4j和LangGraph4j构建AI智能体和工作流，结合Spring Boot 3微服务架构，实现从需求描述到一键部署的全链路自动化。

用户只需输入自然语言（如“创建一个电商网站”），AI即可智能生成完整前端/后端代码，支持可视化编辑、实时预览、一键云部署和企业级管理。

区别于传统低代码工具，本项目深度融合AI工作流和微服务设计，能够处理复杂场景如多文件工程生成和增量修改。

## 项目地址

[AI_Zero_Coder_Studio](https://nondistillable-inaptly-sheila.ngrok-free.dev/)

## 项目展示
![](./Figure/项目展示.jpg)


## 架构图

![](./Figure/架构图.jpg)

### 应用层接口流程图

![](./Figure/应用层接口.png)

### 用户层接口流程图

![](./Figure/用户层接口.png)

### 对话历史接口流程图

![](./Figure/对话历史接口.png)

### 工作流接口流程图

![](./Figure/工作流接口.png)

### AI代码生成器门面类流程图

![](./Figure/并发代码生成工作流.png)

### 代码生成工作流
![](./Figure/代码生成工作流.png)

## 为什么做这个项目？

- **AI革命性创新**：不止生成代码，还支持AI智能路由选择生成策略、工具调用构建复杂项目（如Vue工程），并通过工作流实现多轮交互编辑。

- **企业级架构**：从单体到微服务转型实战，涵盖服务拆分、Dubbo RPC调用、Nacos注册/配置中心、Higress网关（路由/限流/认证），展示高可用、可扩展设计。
- **性能与优化**：多角度优化（性能/安全/稳定性/成本），如虚拟线程响应式编程、AI护轨、TTL对话记忆，适用于高并发AI场景。

## 核心特性

- **智能生成**：自然语言输入 → AI分析策略 → 生成原生HTML/Vue工程，支持流式输出实时反馈。

- **可视化编辑**：实时预览+AI对话修改元素，支持全量/增量更新。

- **一键部署**：自动截图封面、云部署（COS存储）、源码下载和分享链接。

- **企业管理**：用户/应用管理、精选应用设置、AI调用监控、业务指标可视化（Prometheus+Grafana）。

- **AI高级能力**：工作流编排（LangGraph4j）、工具调用、对话记忆持久化（Redis隔离）、护轨防幻觉。

- **监控与优化**：ARMS性能追踪、Token消耗监控、多级缓存、限流熔断。

## 技术栈

### 后端

- **框架**：Spring Boot 3.x + Java 21虚拟线程 + MyBatis Flex。

- **AI核心**：LangChain4j（智能体/工具调用）、LangGraph4j（工作流）、DeepSeek/Qwen模型。

- **微服务**：Spring Cloud Alibaba + Dubbo RPC + Nacos（注册/配置中心） + Higress网关（路由/限流/认证）。

- **存储**：MySQL + Redis（缓存/会话） + COS对象存储 + Caffeine本地缓存。

- **监控**：ARMS（性能/链路追踪） + Prometheus（指标收集） + Grafana（可视化）。

- **工具**：Hutool + Knife4j/Swagger（API文档） + Selenium（网页截图） + Redisson（分布式Session/限流）。

### 前端

- **框架**：Vue 3 + Composition API + Ant Design Vue。

- **工程化**：Vite + TypeScript + ESLint/Prettier + Pinia状态管理 + Axios + OpenAPI代码生成。

- **渲染**：Markdown高亮 + 实时预览组件。

## 更新日志

📝 查看完整更新记录 → [CHANGELOG.md](./CHANGELOG.md)


