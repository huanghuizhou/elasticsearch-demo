package com.hhz.hhztestboot.mybatis;

import tk.mybatis.mapper.common.base.insert.InsertMapper;
import tk.mybatis.mapper.common.base.insert.InsertSelectiveMapper;


/**
 * <p class="detail">
 * 功能:Mysql数据库的Mapper需要继承此Mapper
 * </p>
 *
 * @param <T> the type parameter
 * @author huangHuizhou
 * @ClassName Mybatis mysql mapper.
 * @Version V1.0.
 * @date 2019.03.19 16:00:18
 */
public interface MybatisMysqlMapper<T> extends AbstractCommonMapper<T>, InsertMapper<T>, InsertSelectiveMapper<T> {
}
