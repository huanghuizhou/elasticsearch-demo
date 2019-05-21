package com.hhz.hhztestboot.dao;

import com.hhz.hhztestboot.model.User;
import com.hhz.hhztestboot.mybatis.MybatisMysqlMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends MybatisMysqlMapper<User> {

}