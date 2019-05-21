package com.hhz.hhztestboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@MapperScan({"com.hhz.hhztestboot.dao"})
@SpringBootApplication
public class HhztestBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(HhztestBootApplication.class, args);
    }

}
