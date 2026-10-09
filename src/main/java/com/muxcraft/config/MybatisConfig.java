package com.muxcraft.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置：扫描 mapper 接口
 */
@Configuration
@MapperScan("com.muxcraft.mapper")
public class MybatisConfig {
}