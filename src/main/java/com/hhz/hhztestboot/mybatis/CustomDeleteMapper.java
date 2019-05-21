package com.hhz.hhztestboot.mybatis;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Param;


/**
 * <p class="detail">
 * 功能:自定义删除mapper
 * </p>
 *
 * @param <T> the type parameter
 * @author huangHuizhou
 * @ClassName Custom delete mapper.
 * @Version V1.0.
 * @date 2019.03.19 16:01:36
 */
public interface CustomDeleteMapper<T> {

    /**
     * <p class="detail">
     * 功能:根据主键集合或者数组删除
     * </p>
     *
     * @param key :
     * @return int
     * @author huanghuizhou
     * @date 2019.03.19 16:01:29
     */
    @DeleteProvider(type = CustomDeleteProvider.class, method = "dynamicSQL")
    int deleteByPrimaryKeys(@Param("ids") Object key);
}
