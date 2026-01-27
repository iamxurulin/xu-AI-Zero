package com.ruhuo.xuaizerobackend.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class MyBatisCodeGenerator {
    //需要生成的表名
    private static final String[] TABLE_NAMES = {"user"};
    public static void main(String[] args){
        //获取数据源信息
        Dict dict = YamlUtil.loadByPath("application-local.yml");
        Map<String,Object> dataSourceConfig = dict.getByPath("spring.datasource");
        String url = String.valueOf(dataSourceConfig.get("url"));
        String username = String.valueOf(dataSourceConfig.get("username"));
        String password = String.valueOf(dataSourceConfig.get("password"));

        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        //通过datasource和globalConfig创建代码生成器
        Generator generator = new Generator(dataSource,globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfig(){
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();
        //设置根包，先生成到一个临时目录下，生成代码后，再移动到项目的目录下
        globalConfig.getPackageConfig().setBasePackage("com.ruhuo.xuaizerobackend.genresult");

        //设置表前缀和只生成那些表，setGenerateTable未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                .setLogicDeleteColumn("isDelete");//设置逻辑删除的默认字段名称

        //设置生成entity并启用Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        //设置生成service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();

        //设置生成controller
        globalConfig.enableController();

        //设置生成时间和字符串为空，避免多余的代码改动
        globalConfig.getJavadocConfig()
                .setAuthor("<a href=\"https://github.com/iamxurulin\">iamxurulin</a>")
                .setSince("");
        return globalConfig;
    }
}
