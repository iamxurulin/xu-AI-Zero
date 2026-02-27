package com.ruhuo.xuaizerobackend.constant;

public interface AppConstant {
    /**
     * 精选应用的优先级
     * 定义了精选应用的优先级值为99，具有较高的优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     * 定义了默认应用的优先级值为0，作为普通应用的默认优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     *应用生成目录
     * 定义了代码输出的根目录路径，位于用户工作目录下的tmp/code_output文件夹
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir")+"/tmp/code_output";

    /**
     * 应用部署目录
     * 定义了代码部署的根目录路径，位于用户工作目录下的tmp/code_deploy文件夹
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir")+"/tmp/code_deploy";

    /**
     * 应用部署域名
     * 定义了应用部署时使用的默认主机地址为http://localhost
     */
    String CODE_DEPLOY_HOST = "http://localhost";

}
