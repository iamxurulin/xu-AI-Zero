# 更新日志

## 后端

- 后端初始化，在controller包下新建一个测试接口


- 添加通用基础代码，后端初始化完成

- 完成用户注册功能

完成如下功能：

- 【用户登录】

- 【获取当前登录用户】

- 【用户注销】

- 【用户权限控制】

- 【用户管理】

- fix:雪花算法 ID 传给前端后，后几位变 0 了的精度丢失问题

- 完成原生模式AI应用生成的开发

- 完成流式输出SSE

- 使用【策略模式】、【模板方法模式】、【执行器模式】对【解析器】、【文件保存器】以及【Facade门面类】进行优化


- 完成基础应用能力开发，使用MyBatis Flex生成基础代码；

- 结合Cursor 进行Vibe Coding完成业务代码生成；

实现如下核心功能：

- 【创建应用】

- 【更新应用】

- 【用户删除应用】

- 【用户查看应用详情】

- 【用户分页查询应用】

- 【用户分页查询精选应用】

- 【管理员删除应用】

- 【管理员更新应用】

- 【管理员分页查询应用】

- 【管理员查看应用详情】

使用Cursor结合Vibe Coding以及MyBatis Flex实现以下功能：

- 【对话历史】

- 【关联删除】

- 【游标查询】

- 【管理员查询】

配置Redis，给AI服务方法增加memoryId注解和参数，通过chatMeomryProvider为每个appId分配对话记忆。

- 1.修改AI Service 工厂类，给每个应用分配一个专属的AI Sevice，这样每个AI Service绑定独立的对话记忆。

- 2.引入Caffeine本地缓存优化性能，避免重复构造。

- 3.实现历史对话加载。

- 4.使用Spring Session + Redis管理登录态

- fix： 无法加载对话记忆系统错误，删除AI添加到ChatHistory中的errorMsg字段

- 1.新增 Vue 工程推理流式模型的配置

- 2.新增 写文件工具

- 3.新增 Vue 工程项目的AI生成

- 4.引入 dev 文件夹langchain4j的部分源码，给TokenStream增加工具调用流式输出

- 5.定义统一的流式消息格式

- 6.通过适配器模式的实现将 Vue 生成模式的TokenStream 转为 Flux

- 7.定义消息流处理器，支持对不同模式的消息进行统一处理

- 8.新增 Vue 项目构造器，实现安装完依赖并构建项目

- 9.支持 Vue 项目的部署

- 1.实现本地生成应用封面截图

- 2.保存截图到腾讯云对象存储COS

- 3.新增截图服务SceenshotService


- 完成【下载代码】的前后端开发

- 完成【AI智能选择方案】（html、多文件、Vue工程模式）的前后端开发

- 通过策略模式的思路，实现并优化了
【文件删除工具】、【文件目录读取工具】、【文件修改工具】、【文件读取工具】、【文件写入工具】、【工具管理类】

- 实现Vue工程模式的增量可视化修改

实现以下工具：

- 内容图片收集工具

- 插画图片收集工具

- 架构图绘制工具

- Logo图片生成工具

- 图片收集 AI 服务


完善以下工具：

- 内容图片收集工具

- 插画图片收集工具

- 架构图绘制工具

- Logo图片生成工具

- 图片收集 AI 服务

实现以下节点的开发：

- 图片收集节点

- 提示词增强节点

- 智能路由节点

- 代码生成节点

- 项目构建节点

实现CodeGenWorkflow工作流

- 实现质量检查AI服务

- 开发质量检查工作节点

- modify:代码生成节点、工作流增加质量检查

## 前端

- 前端项目初始化完成，初始页面形成

完成如下页面的开发：

- 【用户登录】

- 【用户注册】

- 【用户注销】

- 【用户管理】

- 【用户权限控制】

使用Cursor结合Vibe Coding实现以下功能：

- 【用户输入提示词创建应用】

- 【AI对话生成应用，并查看效果】

- 【用户查询自己的应用列表】

- 【分页查询精选的应用列表】

- 【查看应用详情】

- 【用户修改自己的应用信息】

- 【用户删除自己的应用】

- 【部署应用】

- 【管理员删除、更新、查看任意应用】

使用Cursor结合Vibe Coding实现以下功能：

【AddChatPage】

- 应用信息加载（fetchAppInfo）

- 聊天历史加载（loadChatHistory，支持游标分页）

- 自动发送 initPrompt（仅拥有者 + 无历史时）

- 用户发送消息 → EventSource 流式接收 AI 响应

- Markdown 渲染（MarkdownRenderer）

- 实时滚动到底部（scrollToBottom）

- 部署应用 + 成功弹窗

- 权限控制（非拥有者禁用输入框和发送按钮）

- 预览 iframe 更新（updatePreview）

【ChatManagePage】

管理员专用的全站聊天记录管理页面

- 搜索表单

- 表格展示

- 分页 + 每页条数切换

- 查看该应用的对话

- 删除单条消息

- 加载时自动请求

- 错误处理

- 新增 Vue 工程模式

- 结合Vibe Coding新增可视化编辑工具文件visualEditor.ts

- 修改对话页面AppChatPage.vue

- 优化原生html和原生多文件模式的提示词

- 实现原生html模式和原生多文件模式的全量可视化修改

## 优化

- 采用多例模式实现AI并发调用

- 精选应用页面采用旁路缓存模式通过Redis缓存优化系统的响应速度

- 通过同步打包实现AI生成完应用后可实时查看网站效果

- 实现基于Redisson的分布式限流

- 通过输入护轨，在用户输入传递给AI模型之前进行检查和过滤

- 为防止AI陷入工具调用的无限循环，设置调用工具的次数上限

- Prometheus + Grafana实现可视化业务监控

- 采用Github+jsDelivr 搭建零成本图床存储截图页面

- 新增对话历史导出功能

fix: 解决类加载器冲突和 openapi2ts 配置问题

1. 后端：禁用 Spring Boot DevTools 修复 ClassCastException
   - 在 pom.xml 中注释掉 spring-boot-devtools 依赖
   - 解决 BaseResponse 被两个不同类加载器加载的问题

2. 前端：修复 ES 模块项目中的 openapi2ts 配置
   - 将 JS 配置文件替换为 JSON 格式 (.openapi2tsrc.json)
   - 解决与 package.json 中 "type": "module" 的兼容性问题

fix: 修复AI返回空响应导致前端卡住和后端异常问题

1. 流式响应处理：修复空响应导致Flux流无法完成
   - AiServiceStreamingResponseHandler.onCompleteResponse 添加 null 检查
   - 空响应时调用 completeResponseHandler 通知下游 sink.complete()
   - 解决前端 EventSource 一直等待、"AI 正在思考..." 无法结束的问题

2. 聊天记录保存：修复空内容触发 BusinessException
   - SimpleTextStreamHandler.doOnComplete 添加 StrUtil.isNotBlank() 检查
   - JsonMessageStreamHandler.doOnComplete 添加同样的空内容校验
   - AI 返回空响应时跳过保存聊天记录，避免 "消息内容不能为空" 异常

3. Vue 项目构建：防止空响应触发构建失败
   - AiCodeGeneratorFacade.onCompleteResponse 添加 response != null 保护
   - 空响应时跳过 vueProjectBuilder.buildProject() 调用
   - 但仍然执行 sink.complete() 确保流正常结束

4. 监听器空指针：全面 null 安全检查
   - AiModelMonitorListener.onError/onResponse/recordTokenUsage/recordResponseTime
   - context、chatRequest、error、metadata 等全部添加 null 判断
   - 解决多处 NullPointerException

5. GitHub 上传：修复 SSL 证书验证失败
   - GithubManager 改用 createUnsafeOkHttpClient()
   - TrustAllCerts + HostnameVerifier 关闭 SSL 验证
   - 解决 javax.net.ssl.SSLHandshakeException

6. 模型配置：暂时禁用监听器避免内部 NPE 传播
   - StreamingChatModelConfig 注释掉 listeners 配置
   - ReasoningStreamingChatModelConfig 同上
   - 后续需排查 langchain4j 内部 null 传递的根本原因
   

feat(frontend): 全面优化首页 UI 视觉效果与交互体验

 1. 首页视觉重构 — 标题与品牌升级
    - 主标题从「AI 应用生成」升级为「让创意瞬间 变为现实」(64px/800weight)
    - 副标题从「AI 零代码应用生成平台」改为「智能代码生成平台」
    - 站点名称优化为 CodeCraftAI，带渐变色 AI 后缀
    - 新增 Hero Badge 标签「🚀 AI 驱动的新一代开发方式」
    - 描述文案重写：强调「自然语言描述 → 完整应用代码」「无需编程基础」

 2. Header 导航栏布局修复
    - 统一 Header 高度为 64px，强制 :deep(.ant-row/.ant-col) 高度对齐
    - Logo 尺寸缩小至 38px，标题 18px/副标题 10px，解决垂直偏移问题
    - 左侧区域设置 height:64px + line-height:1 + align-items:center 确保居中
    - 「智能代码生成平台」副标题右移 8px，优化视觉平衡

 3. 输入交互增强
    - 文本框支持 Enter 键直接创建应用 (@keydown.enter.exact.prevent="createApp")
    - 新增快捷标签栏（个人博客/企业官网/在线商城/作品集）一键填充提示词
    - 输入框聚焦时上浮 + 阴影加深 + 紫色边框高亮

 4. 数据展示区新增统计卡片
    - 新增三列统计数据：10K+ 应用已创建 / 98% 用户满意度 / 30s 平均生成时间
    - 数字采用紫蓝渐变色 + 渐变文字裁剪效果
    - 毛玻璃卡片背景 + 圆角分隔线设计

 5. 动态背景（雪花飞舞）
    - 实现 50 片雪花飘落动画：
      * 大小分层：4px / 5px / 7px / 8px / 9px / 10px 六种规格
      * 速度范围：8~20s，错开延迟 -20s~0s 确保均匀分布
      * 径向渐变填充 (#fff→#e8f0ff→#d0e0ff) 模拟真实雪花质感
      * 三层 box-shadow 发光 (0.9/0.6/0.3) 增强可见度
      * 下落旋转 720° + opacity 全程保持 0.85~1.0m
 6. 后端：强化 HTML 生成 Prompt，解决网页显示原始 Markdown 问题
    - 重写 codegen-html-system-prompt.txt，新增严格输出格式约束：
      * 明确禁止返回任何 Markdown 语法（###、**、- 、> 等）
      * 强制要求全部回复必须且只能是一个 html 完整代码块
      * 要求输出可直接在浏览器中打开运行的完整 HTML 文档
      * 新增最终检查清单（5 项必检），违反即判定失败
    - 补充设计规范参考：美团 Nocode / 百度秒达 / Notion 风格
    - 新增内容处理规范：文字内容必须转为结构化 HTML 元素展示
    

fix: 优化管理页面布局与全局背景效果

- 管理页面（用户/应用/对话）：移除 max-width 限制和多余标题，修复操作列按钮被截断问题

- 表格列移除   fixed 定位（全局 overflow-x:hidden 导致固定列被裁剪）

feat: AI自动生成应用名称 + 内联编辑名称 + 精选缓存同步

- 新增 AiAppNameGeneratorService，创建应用时根据用户描述AI生成简洁应用名称
- AppChatPage 顶部栏支持点击即编辑应用名称，带彩色虚线框交互提示
- updateApp/updateAdmin 接口添加 @CacheEvict，修复精选案例名称不同步问题

feat: 优化"开始创建"按钮交互体验

- 添加全屏加载遮罩，用户点击后立即显示加载状态
- 对话页添加骨架屏，提升数据加载时的用户体验
- 解决点击后长时间等待无反馈的问题

修改文件：
- src/pages/HomePage.vue: 添加全屏加载遮罩和样式
- src/pages/app/AppChatPage.vue: 添加骨架屏加载状态

## 微服务改造

### common公共模块

- common/公共请求响应类(BaseResponse、ResultUtils等)

- constant/常量

- exception/异常处理

- generator/代码生成器

- utils/工具类(除 WebScreenshotUtils外)

- config/配置类(JsonConfig、CorsConfig、CosClientConfig)

- manager/通用能力(CosManager)

- annotation/注解(AuthCheck)


### model实体模型模块

- model/entity/ 实体类(User、App、ChatHistory)

- model/dto/ 数据传输对象

- model/vo/ 视图对象
  
- model/enums/ 枚举类


### client服务接口模块

- 内部截图服务

- 内部使用的用户服务


### user用户服务

- aop/AuthInterceptor.java 权限拦截器

- controller/UserController.java 用户控制器

- service/UserService.java 和 service/impl/UserServiceImpl.java用户服务及实现类

- mapper/UserMapper.java 和对应的resources目录下的 XML 文件


### AI代码生成模块

- dev.langchain4j/ LangChain4j 源码修改

- ai/guardrail/AI 护轨相关代码

- ai/model/AI模型相关代码

- ai/tools/AI工具相关代码

- ai/所有 AI Service

- config/ 和AI模型相关的配置

- resources/prompt/提示词文件

### app应用服务

- ai/ AI 服务工厂类(AiCodeGeneratorServiceFactory.java、AiCodeGenTypeRoutingServiceFactory.java)

- config/ 缓存配置(RedisCacheManagerConfig.java)

- controller/ 控制器层(AppController.java、ChatHistoryController.java、StaticResourceController.java)

- core/ 代码生成核心代码

- mapper/ 数据访问层(AppMapper.java、ChatHistoryMapper.java)

- ratelimit/ 限流模块

- service/ 业务服务层(AppService.java、ChatHistoryService.java、ProjectDownloadService.java及其实现类)

- resources/mapper/ MyBatis映射文件

### screenshot网页截图服务

- utils/WebScreenshotUtils.java 网页截图工具类

- service/ScreenshotService.java 截图服务和实现类

- Nacos + Dubbo服务间调用（user、app、screenshot）

- Higress 微服务网关，修改前端vite为8080端口

- app应用服务引入AOP 鉴权，防止未登录时调用管理员查询对话历史接口
