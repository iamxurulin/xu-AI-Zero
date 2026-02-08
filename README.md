# AI_Zero_Code_Project

### 后端

- 后端初始化，在controller包下新建一个测试接口


- 添加通用基础代码，后端初始化完成

- 完成用户注册功能


### 前端

- 前端项目初始化完成，初始页面形成


### 后端

完成如下功能：

- 【用户登录】

- 【获取当前登录用户】

- 【用户注销】

- 【用户权限控制】

- 【用户管理】

fix:

- 雪花算法 ID 传给前端后，后几位变 0 了的精度丢失问题



### 前端

完成如下页面的开发：

- 【用户登录】

- 【用户注册】
- 【用户注销】

- 【用户管理】

- 【用户权限控制】


### 后端

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



### 前端

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



### 对话历史后端开发：

使用Cursor结合Vibe Coding以及MyBatis Flex实现以下功能：

- 【对话历史】
- 【关联删除】
- 【游标查询】
- 【管理员查询】



### 对话历史前端开发

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

### 对话记忆后端开发

配置Redis，给AI服务方法增加memoryId注解和参数，通过chatMeomryProvider为每个appId分配对话记忆。

- 1.修改AI Service 工厂类，给每个应用分配一个专属的AI Sevice，这样每个AI Service绑定独立的对话记忆。

- 2.引入Caffeine本地缓存优化性能，避免重复构造。

- 3.实现历史对话加载。

- 4.使用Spring Session + Redis管理登录态

fix：

- 无法加载对话记忆系统错误，删除AI添加到ChatHistory中的errorMsg字段



### 后端

- 1.新增 Vue 工程推理流式模型的配置

- 2.新增 写文件工具

- 3.新增 Vue 工程项目的AI生成

- 4.引入 dev 文件夹langchain4j的部分源码，给TokenStream增加工具调用流式输出

- 5.定义统一的流式消息格式

- 6.通过适配器模式的实现将 Vue 生成模式的TokenStream 转为 Flux

- 7.定义消息流处理器，支持对不同模式的消息进行统一处理

- 8.新增 Vue 项目构造器，实现安装完依赖并构建项目

- 9.支持 Vue 项目的部署

### 前端

- 新增 Vue 工程模式

### 后端

- 1.实现本地生成应用封面截图

- 2.保存截图到腾讯云对象存储COS

- 3.新增截图服务SceenshotService


- 完成【下载代码】的前后端开发

- 完成【AI智能选择方案】（html、多文件、Vue工程模式）的前后端开发


### 前端

- 结合Vibe Coding新增可视化编辑工具文件visualEditor.ts

- 修改对话页面AppChatPage.vue

- 优化原生html和原生多文件模式的提示词

- 实现原生html模式和原生多文件模式的全量可视化修改

### 后端

- 通过策略模式的思路，实现并优化了
【文件删除工具】、【文件目录读取工具】、【文件修改工具】、【文件读取工具】、【文件写入工具】、【工具管理类】

- 实现Vue工程模式的增量可视化修改


### 后端
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

modify:

代码生成节点、工作流增加质量检查


### 优化

- 采用多例模式实现AI并发调用

- 精选应用页面采用旁路缓存模式通过Redis缓存优化系统的响应速度

- 通过同步打包实现AI生成完应用后可实时查看网站效果

- 实现基于Redisson的分布式限流

- 通过输入护轨，在用户输入传递给AI模型之前进行检查和过滤

- 为防止AI陷入工具调用的无限循环，设置调用工具的次数上限

- Prometheus + Grafana实现可视化业务监控



### 微服务改造

#### common公共模块

- common/公共请求响应类(BaseResponse、ResultUtils等)

- constant/常量

- exception/异常处理

- generator/代码生成器

- utils/工具类(除 WebScreenshotUtils外)

- config/配置类(JsonConfig、CorsConfig、CosClientConfig)

- manager/通用能力(CosManager)

- annotation/注解(AuthCheck)


#### model实体模型模块

- model/entity/ 实体类(User、App、ChatHistory)

- model/dto/ 数据传输对象

- model/vo/ 视图对象
  
- model/enums/ 枚举类


#### client服务接口模块

- 内部截图服务

- 内部使用的用户服务


#### user用户服务

- aop/AuthInterceptor.java 权限拦截器

- controller/UserController.java 用户控制器

- service/UserService.java 和 service/impl/UserServiceImpl.java用户服务及实现类

- mapper/UserMapper.java 和对应的resources目录下的 XML 文件



#### AI代码生成模块

- dev.langchain4j/ LangChain4j 源码修改

- ai/guardrail/AI 护轨相关代码

- ai/model/AI模型相关代码

- ai/tools/AI工具相关代码

- ai/所有 AI Service

- config/ 和AI模型相关的配置

- resources/prompt/提示词文件