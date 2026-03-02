package dev.langchain4j.internal;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.langchain4j.internal.Utils.isNotNullOrBlank;
import static dev.langchain4j.internal.Utils.isNotNullOrEmpty;

/**
 * 这是一个工具执行请求构建器类，用于构建工具执行请求对象。
 * 该类使用构建器模式来创建和配置ToolExecutionRequest对象。
 */
@Internal
public class ToolExecutionRequestBuilder {

    // 使用原子引用来存储索引值，确保线程安全
    private final AtomicReference<Integer> index;

    // 使用原子引用存储工具ID和名称，确保线程安全
    private final AtomicReference<String> id = new AtomicReference<>();
    private final AtomicReference<String> name = new AtomicReference<>();
    // 使用StringBuffer来构建参数字符串
    private final StringBuffer arguments = new StringBuffer();

    // 存储所有构建的工具执行请求
    private final List<ToolExecutionRequest> allToolExecutionRequests = new ArrayList<>();

    /**
     * 默认构造函数，初始化索引为0
 * 该构造函数不带参数，调用带参构造函数并传入初始值0
     */
    public ToolExecutionRequestBuilder() { // 默认构造函数，无参数
        this(0); // 调用带参构造函数，将索引初始化为0
    }

    /**
     * 带索引参数的构造函数
     * 初始化ToolExecutionRequestBuilder实例，设置初始索引值
     *
     * @param index 初始索引值，用于跟踪请求的执行顺序
     */
    public ToolExecutionRequestBuilder(int index) {
        // 使用AtomicReference包装index参数，确保线程安全
        this.index = new AtomicReference(index);
    }

    /**
     * 获取当前索引值
     * 该方法用于获取当前对象的索引值
     *
     * @return 当前索引值 返回当前对象的索引值，类型为int
     */
    public int index() {
        // 使用get()方法获取index的当前值
        return index.get();
    }

    /**
     * 更新索引值
     * 该方法用于更新当前的索引值，如果传入的参数不为null，则将当前索引值更新为传入的值，并返回更新后的索引值。
     *
     * @param index 新的索引值，如果为null则不更新
     * @return 更新后的索引值
     */
    public int updateIndex(Integer index) {
        // 检查传入的index是否为null
        if (index != null) {
            // 如果index不为null，则更新当前索引值为传入的值
            this.index.set(index);
        }
        // 返回当前的索引值
        return this.index.get();
    }

    /**
     * 获取当前ID
     *
     * @return 当前ID
     */
    public String id() {  // 定义一个公共方法，用于获取当前ID
        return id.get();  // 返回id字段的当前值
    }

    /**
     * 更新ID值
     * 该方法用于更新对象的ID属性，提供了参数校验功能，确保只有非空的ID才会被更新
     *
     * @param id 新的ID值，如果为null或空字符串则不更新
     * @return 更新后的ID值，如果输入无效则返回当前值
     */
    public String updateId(String id) {
        // 使用isNotNullOrBlank方法检查参数是否为null或空字符串
        // 只有当参数有效时才会执行更新操作
        if (isNotNullOrBlank(id)) {
            // 调用id对象的set方法更新值
            this.id.set(id);
        }
        // 返回当前id对象的值
        return this.id.get();
    }

    /**
     * 获取名称的方法
     *
     * @return 返回名称属性值
     */
    public String name() {
        return name.get(); // 返回name对象的值
    }

    /**
     * 更新名称的方法
     *
     * @param name 要更新的新名称
     * @return 更新后的名称
     */
    public String updateName(String name) {
        // 检查名称是否为null或空字符串
        if (isNotNullOrBlank(name)) {
            // 如果名称有效，则更新当前名称
            this.name.set(name);
        }
        // 返回更新后的名称
        return this.name.get();
    }

    /**
     * 将部分参数追加到现有参数字符串中
     *
     * @param partialArguments 要追加的部分参数字符串
     */
    public void appendArguments(String partialArguments) {
        // 检查输入参数是否为非空且非空字符串
        if (isNotNullOrEmpty(partialArguments)) {
            // 将部分参数追加到arguments字符串缓冲区中
            arguments.append(partialArguments);
        }
    }

    /**
     * 构建并返回一个ToolExecutionRequest对象，同时将构建的对象添加到allToolExecutionRequests集合中
     *
     * @return 返回构建好的ToolExecutionRequest对象
     */
    public ToolExecutionRequest build() {
        // 将StringBuffer类型的arguments转换为String
        String arguments = this.arguments.toString();
        // 使用Builder模式构建ToolExecutionRequest对象
        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                // 设置请求ID
                .id(id.get())
                // 设置请求名称
                .name(name.get())
                // 设置请求参数，如果参数为空则使用空JSON对象"{}"
                .arguments(arguments.isEmpty() ? "{}" : arguments)
                // 构建对象
                .build();
        // 将构建的请求对象添加到集合中
        allToolExecutionRequests.add(toolExecutionRequest);
        // 重置当前构建器的状态，以便重用
        reset();
        // 返回构建好的请求对象
        return toolExecutionRequest;
    }

    /**
     * 重置方法，用于将对象的各个属性恢复到初始状态
     * 将id、name设置为null，arguments清空
     */
    private void reset() {
        // 将id属性设置为null
        id.set(null);
        // 将name属性设置为null
        name.set(null);
        // 清空arguments中的内容
        arguments.setLength(0);
    }

    /**
     * 检查是否有工具执行请求
     * 该方法用于判断是否存在待处理的工具执行请求
     *
     * @return 如果存在工具执行请求或名称不为空，则返回true；否则返回false
     */
    public boolean hasToolExecutionRequests() {
        // 检查工具执行请求列表是否为空，或者名称是否不为空
        // 如果任一条件为真，则表示存在待处理的请求
        return !allToolExecutionRequests.isEmpty() || name.get() != null;
    }

    /**
     * 获取所有工具执行请求的方法
     *
     * @return 返回包含所有工具执行请求的列表
     */
    public List<ToolExecutionRequest> allToolExecutionRequests() {
        // 直接返回存储所有工具执行请求的列表
        return allToolExecutionRequests;
    }
}