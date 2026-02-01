# AI_Zero_Code_Project


### 后端

1.完成基础应用能力开发，使用MyBatis Flex生成基础代码；

2.结合Cursor 进行Vibe Coding完成业务代码生成；
实现如下核心功能：
【创建应用】、【更新应用】、【用户删除应用】、
【用户查看应用详情】、【用户分页查询应用】、
【用户分页查询精选应用】、【管理员删除应用】、
【管理员更新应用】、【管理员分页查询应用】、
【管理员查看应用详情】。



### 前端

使用Cursor结合Vibe Coding实现以下功能：
【用户输入提示词创建应用】、
【AI对话生成应用，并查看效果】、
【用户查询自己的应用列表】、
【分页查询精选的应用列表】、
【查看应用详情】、
【用户修改自己的应用信息】、
【用户删除自己的应用】、
【部署应用】、
【管理员删除、更新、查看任意应用】



### 对话历史后端开发：

使用Cursor结合Vibe Coding以及MyBatis Flex实现以下功能：

【对话历史】、
【关联删除】、
【游标查询】、
【管理员查询】



### 对话历史前端开发

使用Cursor结合Vibe Coding实现以下功能：
【AddChatPage】
● 应用信息加载（fetchAppInfo）
● 聊天历史加载（loadChatHistory，支持游标分页）
● 自动发送 initPrompt（仅拥有者 + 无历史时）
● 用户发送消息 → EventSource 流式接收 AI 响应
● Markdown 渲染（MarkdownRenderer）
● 实时滚动到底部（scrollToBottom）
● 部署应用 + 成功弹窗
● 权限控制（非拥有者禁用输入框和发送按钮）
● 预览 iframe 更新（updatePreview）

【ChatManagePage】
管理员专用的全站聊天记录管理页面
● 搜索表单
● 表格展示
● 分页 + 每页条数切换
● 查看该应用的对话
● 删除单条消息
● 加载时自动请求
● 错误处理

### 对话记忆后端开发

配置Redis，给AI服务方法增加memoryId注解和参数，通过chatMeomryProvider为每个appId分配对话记忆。

1.修改AI Service 工厂类，给每个应用分配一个专属的AI Sevice，这样每个AI Service绑定独立的对话记忆。

2.引入Caffeine本地缓存优化性能，避免重复构造。

3.实现历史对话加载。

4.使用Spring Session + Redis管理登录态

fix：

无法加载对话记忆系统错误，删除AI添加到ChatHistory中的errorMsg字段