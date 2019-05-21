package com.hhz.hhztestboot.dao;

import com.hhz.hhztestboot.model.User;
import com.uniubi.sdk.mybatis.MybatisMysqlMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends MybatisMysqlMapper<User> {

}