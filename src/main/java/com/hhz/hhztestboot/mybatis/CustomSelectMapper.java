package com.hhz.hhztestboot.mybatis;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;


/**
 * <p class="detail">
 * 功能:自定义查询mapper
 * </p>
 *
 * @param <T> the type parameter
 * @author huangHuizhou
 * @ClassName Custom select mapper.
 * @Version V1.0.
 * @date 2019.03.19 16:01:18
 */
public interface CustomSelectMapper<T> {

    /**
     * <p class="detail">
     * 功能:根据主键集合或数组查询,如果传入空值,则返回整张表全部结果
     * </p>
     *
     * @param ids :
     * @return list
     * @author huanghuizhou
     * @date 2019.03.19 16:01:10
     */
    @SelectProvider(type = CustomSelectProvider.class, method = "dynamicSQL")
    List<T> selectByPrimaryKeys(@Param("ids") Object ids);
}
