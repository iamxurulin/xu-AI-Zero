package com.ruhuo.xuaizerobackend.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

/**
 * MyBatis代码生成器类
 * 用于根据数据库表自动生成MyBatis相关的代码文件，包括实体类、Mapper接口、Service层、Controller等
 */
public class MyBatisCodeGenerator {
    //需要生成的表名数组
    private static final String[] TABLE_NAMES = {"chat_history"};

    /**
     * 程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 从YAML配置文件中加载数据库配置
        Dict dict = YamlUtil.loadByPath("application-local.yml");
        // 从配置中获取Spring数据源配置信息
        Map<String, Object> dataSourceConfig = dict.getByPath("spring.datasource");

        // 从数据源配置中提取数据库连接信息
        String url = String.valueOf(dataSourceConfig.get("url"));  // 数据库连接URL
        String username = String.valueOf(dataSourceConfig.get("username"));  // 数据库用户名
        String password = String.valueOf(dataSourceConfig.get("password"));  // 数据库密码

        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();  // 创建HikariCP数据源实例
        dataSource.setJdbcUrl(url);  // 设置数据库连接URL
        dataSource.setUsername(username);  // 设置数据库用户名
        dataSource.setPassword(password);  // 设置数据库密码

        // 创建全局配置对象
        GlobalConfig globalConfig = createGlobalConfig();

        //通过datasource和globalConfig创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        // 执行代码生成操作
        generator.generate();
    }

    /**
     * 创建全局配置对象
     * 用于配置代码生成器的各项参数，包括包名、表名、生成选项等
     *
     * @return 返回配置完成的GlobalConfig对象
     */
    public static GlobalConfig createGlobalConfig() {
        //创建配置内容 - 实例化GlobalConfig对象
        GlobalConfig globalConfig = new GlobalConfig();
        //设置根包，先生成到一个临时目录下，生成代码后，再移动到项目的目录下
        globalConfig.getPackageConfig().setBasePackage("com.ruhuo.xuaizerobackend.genresult");

        //设置表前缀和只生成那些表，setGenerateTable未配置时，生成所有表
        //设置逻辑删除的默认字段名称
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                .setLogicDeleteColumn("isDelete");//设置逻辑删除的默认字段名称

        //设置生成entity并启用Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)    // 启用Lombok支持，简化实体类代码
                .setJdkVersion(21);     // 指定使用的JDK版本为21

        // 启用Mapper接口的生成
        globalConfig.enableMapper();
        // 启用Mapper XML文件的生成
        globalConfig.enableMapperXml();

        // 启用服务层功能
        globalConfig.enableService();
        // 启用服务层实现功能
        globalConfig.enableServiceImpl();


        // 启用控制器层功能
        globalConfig.enableController();


        // 配置Java文档注释信息
        // 设置作者信息，链接到GitHub个人主页
        // 设置since字段为空
        globalConfig.getJavadocConfig()
                .setAuthor("<a href=\"https://github.com/iamxurulin\">iamxurulin</a>")
                .setSince("");
        // 返回全局配置对象
        return globalConfig;
    }
}
