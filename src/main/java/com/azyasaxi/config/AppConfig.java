package com.azyasaxi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment; // 用于访问属性文件
import org.springframework.jdbc.datasource.DriverManagerDataSource; // 一个简单的 DataSource 实现

import javax.sql.DataSource; // 标准 JDBC DataSource 接口

/**
 * Spring 核心配置类 (AppConfig)
 * 负责定义应用程序的 beans、组件扫描规则以及其他配置。
 */
@Configuration // 声明这是一个 Spring 配置类
@ComponentScan(basePackages = "com.azyasaxi") // 扫描 "com.azyasaxi" 包及其子包下的 Spring 组件 (如 @Component, @Service, @Repository)
@PropertySource("classpath:application.properties") // 加载位于 classpath 下的 application.properties 文件
public class AppConfig {

    // Spring Environment 会被自动注入，用于访问属性文件中的值
    private final Environment env;

    // 通过构造函数注入 Environment
    public AppConfig(Environment env) {
        this.env = env;
    }

    /**
     * 配置并返回一个 DataSource bean。
     * DataSource 用于管理数据库连接。
     * 配置信息从 application.properties 文件中读取。
     *
     * @return 配置好的 DataSource 实例
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        // 从 application.properties 文件中获取数据库驱动类名
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        // 从 application.properties 文件中获取数据库连接 URL
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        // 从 application.properties 文件中获取数据库用户名
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        // 从 application.properties 文件中获取数据库密码
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        return dataSource;
    }

    /**
     * 配置并返回一个 JdbcTemplate bean。
     * JdbcTemplate 简化了 JDBC 操作，自动处理资源的打开和关闭。
     *
     * @param dataSource Spring 管理的 DataSource bean，将自动注入。
     * @return 配置好的 JdbcTemplate 实例。
     */
    @Bean
    public org.springframework.jdbc.core.JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new org.springframework.jdbc.core.JdbcTemplate(dataSource);
    }

}