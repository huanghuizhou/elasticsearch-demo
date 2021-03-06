package com.hhz.hhztestboot.mybatis;


import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

import java.util.Iterator;
import java.util.Set;


public class CustomDeleteProvider extends MapperTemplate {
    /**
     * Instantiates a new Custom delete provider.
     *
     * @param mapperClass  the mapper class
     * @param mapperHelper the mapper helper
     */
    public CustomDeleteProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    public String deleteByPrimaryKeys(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.deleteFromTable(entityClass, tableName(entityClass)));
        sql.append("where ");
        sql.append("<foreach collection=\"ids\" item=\"id\" separator=\" or \" open=\"(\" close=\")\">");
        Set<EntityColumn> columnList = EntityHelper.getPKColumns(entityClass);
        Iterator columnListIterator = columnList.iterator();
        int idx = 0;
        int pkSize = columnList.size();//pk size
        if (pkSize == 1) {
            EntityColumn column = (EntityColumn) columnListIterator.next();
            sql.append(column.getColumnEqualsHolder());
        } else {
            while (columnListIterator.hasNext()) {
                EntityColumn column = (EntityColumn) columnListIterator.next();
                if (idx == 0) {
                    sql.append("(" + column.getColumnEqualsHolder("id"));
                } else {
                    sql.append(" and " + column.getColumnEqualsHolder("id"));
                }
                if (idx == pkSize - 1) {
                    sql.append(")");
                }
                idx++;
            }
        }
        sql.append("</foreach>");
        return sql.toString();
    }
}
