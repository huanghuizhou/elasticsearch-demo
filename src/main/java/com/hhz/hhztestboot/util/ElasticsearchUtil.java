package com.hhz.hhztestboot.util;

import com.alibaba.fastjson.JSONObject;
import com.hhz.hhztestboot.enums.EsQueryTypeEnum;
import com.hhz.hhztestboot.model.Account;
import com.hhz.hhztestboot.model.MedusaEsPage;
import com.hhz.hhztestboot.model.entity.SearchField;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.Id;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p class="detail">
 * 功能:
 * </p>
 *
 * @author HuangHuizhou
 * @ClassName Elasticsearch util.
 * @Version V1.0.
 * @date 2019.05.18 16:18:03
 */
@Component
public class ElasticsearchUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchUtil.class);

    private final TransportClient transportClient;

    private static TransportClient client;

    //高亮标签
    private static final String PRE_TAG = "<span style='color:red' >";
    private static final String POST_TAG = "</span>";

    @Autowired
    public ElasticsearchUtil(TransportClient transportClient) {
        this.transportClient = transportClient;
    }

    @PostConstruct
    public void init() {
        client = this.transportClient;
    }

    /**
     * 创建索引
     *
     * @param index :
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 16:18:03
     */
    public static boolean createIndex(String index,boolean ifIk)  {
        if (!isIndexExist(index)) {
            LOGGER.info("Index is not exits!");
        }
        CreateIndexResponse indexresponse = client.admin().indices().prepareCreate(index).execute().actionGet();
        LOGGER.info("执行建立成功？" + indexresponse.isAcknowledged());
        if(!indexresponse.isAcknowledged()){
            return false;
        }
        if(!ifIk){
            return true;
        }
        //创建mapping 使用中文分词
        boolean mapping = createMapping(index, Account.class);
        if(!mapping){
            deleteIndex(index);
        }
        return mapping;

    }

    /**
     * demo mapping
     * {
     *     "medusa-es": {
     *         "mappings": {
     *             "account": {
     *                 "properties": {
     *                     "date": {
     *                         "type": "date"
     *                     },
     *                     "employeeNumber": {
     *                         "type": "text",
     *                         "fields": {
     *                             "keyword": {
     *                                 "type": "keyword",
     *                                 "ignore_above": 256
     *                             }
     *                         },
     *                         "analyzer": "ik_max_word",
     *                         "search_analyzer": "ik_smart"
     *                     },
     *                     "id": {
     *                         "type": "text",
     *                         "index_options": "docs"
     *                     },
     *                     "name": {
     *                         "type": "text",
     *                         "fields": {
     *                             "keyword": {
     *                                 "type": "keyword",
     *                                 "ignore_above": 256
     *                             }
     *                         },
     *                         "analyzer": "ik_max_word",
     *                         "search_analyzer": "ik_smart"
     *                     },
     *                     "position": {
     *                         "type": "text",
     *                         "fields": {
     *                             "keyword": {
     *                                 "type": "keyword",
     *                                 "ignore_above": 256
     *                             }
     *                         },
     *                         "analyzer": "ik_max_word",
     *                         "search_analyzer": "ik_smart"
     *                     },
     *                     "stageName": {
     *                         "type": "text",
     *                         "fields": {
     *                             "keyword": {
     *                                 "type": "keyword",
     *                                 "ignore_above": 256
     *                             }
     *                         },
     *                         "analyzer": "ik_max_word",
     *                         "search_analyzer": "ik_smart"
     *                     },
     *                     "type": {
     *                         "type": "long"
     *                     }
     *                 }
     *             }
     *         }
     *     }
     * }
     */
    public static boolean createMapping(String index,Class cls){
        String type=cls.getSimpleName().toLowerCase();
        List<Field> fields = ReflectionUtil.getAllFieldsList(
                cls,
                false,
                q -> q.isAnnotationPresent(Id.class) || q.isAnnotationPresent(org.springframework.data.annotation.Id.class),
                true);
        Field idField;
        if (fields.size() > 0) {
            idField = fields.get(0);
        } else {
            idField = ReflectionUtil.getFieldByName(cls, "id");
        }

        if(idField==null){
            throw new RuntimeException("es id can not be null");
        }

        String id=idField.getName();


        List<Field> allFieldsList = ReflectionUtil.getAllFieldsList(cls,true,
                q -> !q.getName().equals(idField.getName()),
                false);
        XContentBuilder builder = null;
        try {
            builder = XContentFactory.jsonBuilder().startObject()
                    .startObject(type).startObject("properties");
            builder.startObject(id).field("type", "text").field("index_options","docs").endObject();

            for(Field field:allFieldsList){
                //string 类型做中文分词
                if(String.class == field.getType()){
                    builder.startObject(field.getName()).field("type", "text") .field("analyzer","ik_max_word").field("search_analyzer","ik_max_word").startObject("fields").startObject("keyword").field("type","keyword").field("ignore_above",256).endObject().endObject().endObject();
                }
            }
            builder.endObject().endObject().endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PutMappingRequest mapping = Requests.putMappingRequest(index).type(type).source(builder);
        PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        return putMappingResponse.isAcknowledged();
    }


    /**
     * 删除索引
     *
     * @param index :
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 16:18:03
     */
    public static boolean deleteIndex(String index) {
        if (!isIndexExist(index)) {
            LOGGER.info("Index is not exits!");
        }
        DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
        if (dResponse.isAcknowledged()) {
            LOGGER.info("delete index " + index + "  successfully!");
        } else {
            LOGGER.info("Fail to delete index " + index);
        }
        return dResponse.isAcknowledged();
    }

    /**
     * 判断索引是否存在
     *
     * @param index :
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 16:18:03
     */
    public static boolean isIndexExist(String index) {
        IndicesExistsResponse inExistsResponse = client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet();
        if (inExistsResponse.isExists()) {
            LOGGER.info("Index [" + index + "] is exist!");
        } else {
            LOGGER.info("Index [" + index + "] is not exist!");
        }
        return inExistsResponse.isExists();
    }

    /**
     * <p class="detail">
     * 功能:
     * </p>
     *
     * @param index :
     * @param type  :
     * @return boolean
     * @author huanghuizhou
     * @date 2019.05.18 16:18:03
     * @Author: LX
     * @Description: 判断inde下指定type是否存在
     * @Date: 2018 /11/6 14:46
     * @Modified by :
     */
    public boolean isTypeExist(String index, String type) {
        return isIndexExist(index)
                ? client.admin().indices().prepareTypesExists(index).setTypes(type).execute().actionGet().isExists()
                : false;
    }


    /**
     * <p class="detail">
     * 功能:新增数据
     * </p>
     *
     * @param <T>   the type parameter
     * @param data  :
     * @param index :
     * @param type  :
     * @return string
     * @author huanghuizhou
     * @date 2019.05.20 17:26:36
     */
    public static<T> String addData(T data, String index, String type) {
        String id = getId(data);
        JSONObject jsonObject=(JSONObject)JSONObject.toJSON(data);
        IndexResponse response = client.prepareIndex(index, type, id).setSource(jsonObject).get();
        LOGGER.info("addData response status:{},id:{}", response.status().getStatus(), response.getId());
        return response.getId();
    }

    /**
     * 通过ID删除数据
     *
     * @param index 索引，类似数据库
     * @param type  类型，类似表
     * @param id    数据ID
     * @author huanghuizhou
     * @date 2019.05.18 16:18:04
     */
    public static void deleteDataById(String index, String type, String id) {
        DeleteResponse response = client.prepareDelete(index, type, id).execute().actionGet();
        LOGGER.info("deleteDataById response status:{},id:{}", response.status().getStatus(), response.getId());
    }



    /**
     * 通过ID 更新数据
     *
     * @param data       要增加的数据
     * @param index      索引，类似数据库
     * @param type       类型，类似表
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 16:18:04
     */
    public static<T> void updateDataById(T data, String index, String type) {
        String id = getId(data);
        JSONObject jsonObject=(JSONObject)JSONObject.toJSON(data);
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(index).type(type).id(id).doc(jsonObject);
        client.update(updateRequest);
    }

    /**
     * 通过ID获取数据
     *
     * @param <T>   the type parameter
     * @param index 索引，类似数据库
     * @param type  类型，类似表
     * @param id    数据ID
     * @param clazz :
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 16:18:04
     */
    public static<T> T searchDataById(String index, String type, String id, Class<T> clazz) {
        if(StringUtils.isEmpty(id)){
            throw new RuntimeException("id can not be null");
        }
        if(clazz==null){
            throw new RuntimeException("class can not be null");
        }
        GetRequestBuilder getRequestBuilder = client.prepareGet(index, type, id);

        //缺省为全部字段
        //getRequestBuilder.setFetchSource(fields.split(","), null);

        String sourceStr = getRequestBuilder.execute().actionGet().getSourceAsString();
        if(StringUtils.isEmpty(sourceStr)){
            return null;
        }
        return GsonUtil.getEsGson().fromJson(sourceStr,clazz);
    }



    /**
     * 使用分词查询,并分页（权重都为1）
     *
     * @param <T>         the type parameter
     * @param index       索引名称
     * @param type        类型名称,可传入多个type逗号分隔
     * @param startPage   当前页
     * @param pageSize    每页显示条数
     * @param keyWord     查询内容
     * @param sortField   排序字段
     * @param clazz       序列化对象类型
     * @param matchFields 匹配的字段
     * @return page es page
     * @author huanghuizhou
     * @date 2019.05.18 16:18:04
     */
    public static<T>  MedusaEsPage<T> searchEs(String index, String type, int startPage, int pageSize, String keyWord, String sortField, Class<T>  clazz, EsQueryTypeEnum esQueryTypeEnum, boolean highShow, String... matchFields) {
        //matchFields=new String[]{"name","stageName","position","employeeNumber"};
        return doSearchEs(index,type,startPage,pageSize,keyWord,sortField,clazz, esQueryTypeEnum,highShow,toSearchFields(matchFields));
    }

    /**
     * <p class="detail">
     * 功能:使用分词查询,并分页（自定义权重）
     * </p>
     *
     * @param <T>          the type parameter
     * @param index        :
     * @param type         :
     * @param startPage    :
     * @param pageSize     :
     * @param keyWord      :
     * @param sortField    :
     * @param clazz        :
     * @param searchFields :
     * @return medusa es page
     * @author huanghuizhou
     * @date 2019.05.20 18:23:21
     */
    //自定义权重
    public static<T>  MedusaEsPage<T> searchEs(String index, String type, int startPage, int pageSize, String keyWord, String sortField, Class<T>  clazz, EsQueryTypeEnum esQueryTypeEnum,boolean highShow,SearchField... searchFields) {
        return doSearchEs(index,type,startPage,pageSize,keyWord,sortField,clazz,esQueryTypeEnum,highShow,searchFields);
    }

    private static<T>  MedusaEsPage<T> doSearchEs(String index, String type, int startPage, int pageSize, String keyWord, String sortField, Class<T>  clazz, EsQueryTypeEnum esQueryTypeEnum,boolean highShow,SearchField[] searchFields) {
        if (searchFields == null || searchFields.length == 0) {
            throw new RuntimeException("searchFields not exists");
        }
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
        if (StringUtils.isNotEmpty(type)) {
            searchRequestBuilder.setTypes(type);
        }
        QueryBuilder queryBuilder=getQuery(esQueryTypeEnum,keyWord,searchFields);
        searchRequestBuilder.setQuery(queryBuilder);
        searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH);
        // 需要显示的字段，逗号分隔（缺省为全部字段）
        //searchRequestBuilder.setFetchSource(fields, null);
        //排序字段
        if (StringUtils.isNotEmpty(sortField)) {
            searchRequestBuilder.addSort(sortField, SortOrder.DESC);
        }
        //设置高亮
        if(highShow){
            highlight(searchRequestBuilder,searchFields);
        }
        // 分页设置 这里setForm 指的是跳过的个数 不是 startPage
        searchRequestBuilder.setFrom(startPage*pageSize).setSize(pageSize);

        // 设置是否按查询匹配度排序
        searchRequestBuilder.setExplain(true);

        //打印的内容 可以在 Elasticsearch head 和 Kibana  上执行查询
        LOGGER.info("\n{}", searchRequestBuilder);

        // 执行搜索,返回搜索响应信息
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        long totalHits = searchResponse.getHits().totalHits;
        long length = searchResponse.getHits().getHits().length;

        LOGGER.debug("共查询到[{}]条数据,处理数据条数[{}]", totalHits, length);

        if (searchResponse.status().getStatus() == 200) {
            // 解析对象
            List<T> recordList = parseSearchResponse(searchResponse, clazz,highShow);

            return new MedusaEsPage(startPage, pageSize, (int) totalHits, recordList);
        }
        return null;
    }


    //设置高亮
    private static void highlight(SearchRequestBuilder searchRequestBuilder, SearchField[] searchFields) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //设置前缀
        highlightBuilder.preTags(PRE_TAG);
        //设置后缀
        highlightBuilder.postTags(POST_TAG);

        // 设置高亮字段
        for(SearchField field:searchFields){
            highlightBuilder.field(field.getField());
        }
        searchRequestBuilder.highlighter(highlightBuilder);
    }

    private static QueryBuilder getQuery(EsQueryTypeEnum esQueryTypeEnum,String keyWord,SearchField[] searchFields){
        if(esQueryTypeEnum==null){
            throw new RuntimeException("es query type can not be null");
        }
        if(EsQueryTypeEnum.match==esQueryTypeEnum){
            return getMatchQuery(keyWord,searchFields);
        }
        if(EsQueryTypeEnum.matchPhrase==esQueryTypeEnum){
            return getBoolMatchPhraseQuery(keyWord,searchFields);
        }
        if(EsQueryTypeEnum.term==esQueryTypeEnum){
            return getBoolTermQuery(keyWord,searchFields);
        }
        throw new RuntimeException("es query type mapping failed");
    }


    //匹配查询
    private static QueryBuilder getMatchQuery(String keyWord,SearchField[] searchFields){
        String[] fields = Arrays.stream(searchFields).map(SearchField::getField).collect(Collectors.toList()).toArray(new String[searchFields.length]);
        if(searchFields.length==1){
            return QueryBuilders.matchQuery(searchFields[0].getField(), keyWord);
        }

        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyWord,fields);
        //MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("stageName", keyWord);
        //设置权重
        for (SearchField searchField : searchFields) {
            multiMatchQueryBuilder.field(searchField.getField(), searchField.getWeight());
        }
        //中文分词
        //multiMatchQueryBuilder.analyzer("ik_smart");
        return multiMatchQueryBuilder;
    }


    //精准匹配
    private static QueryBuilder getBoolTermQuery(String keyWord,SearchField[] searchFields){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for(SearchField searchField:searchFields){
            boolQueryBuilder.should(QueryBuilders.termQuery(searchField.getField(),keyWord)).boost(searchField.getWeight());
        }
        return boolQueryBuilder;
    }

    //整词匹配
    private static QueryBuilder getBoolMatchPhraseQuery(String keyWord,SearchField[] searchFields){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for(SearchField searchField:searchFields){
            boolQueryBuilder.should(QueryBuilders.matchPhraseQuery(searchField.getField(),keyWord)).boost(searchField.getWeight());
        }
        return boolQueryBuilder;
    }

    private static<T> List<T> parseSearchResponse(SearchResponse response, Class<T> entityClass,boolean highShow) {
        List<T> list = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            String source;
            if(highShow){
                for(Map.Entry<String,HighlightField> entry:hit.getHighlightFields().entrySet()){
                    StringBuilder highStr=new StringBuilder();
                    Text[] text = entry.getValue().getFragments();

                    if (text != null) {
                        for (Text str : text) {
                            highStr.append(str.string());
                        }
//遍历 高亮结果集，覆盖 正常结果集
                        hit.getSourceAsMap().put(entry.getKey(), highStr.toString());
                    }
                }

                source = GsonUtil.getEsGson().toJson(hit.getSourceAsMap());
            }else {
                source=hit.getSourceAsString();
            }
            T t = GsonUtil.getEsGson().fromJson(source, entityClass);
            list.add(t);
        }
        return list;
    }


    //获取id
    private static<T> String getId(T data){
        List<Field> fields = ReflectionUtil.getAllFieldsList(
                data.getClass(),
                false,
                q -> q.isAnnotationPresent(Id.class) || q.isAnnotationPresent(org.springframework.data.annotation.Id.class),
                true);
        Field idField;
        if (fields.size() > 0) {
            idField = fields.get(0);
        } else {
            idField = ReflectionUtil.getFieldByName(data.getClass(), "id");
        }

        if (idField == null) {
            throw new RuntimeException("Id field not found");
        }
        Object bizId = ReflectionUtil.getValueFromField(idField, data);

        if (bizId == null) {
            throw new RuntimeException("Id cannot be null");
        }
        return bizId.toString();
    }

    //查询字段设置权重
    private static SearchField[] toSearchFields(String... searchFields) {
        if(searchFields==null||searchFields.length==0){
            throw new RuntimeException("search field can not be null");
        }
        return Arrays.stream(searchFields).map(q -> {
            SearchField field = new SearchField();
            field.setField(q);
            field.setWeight(1);
            return field;
        }).collect(Collectors.toList()).toArray(new SearchField[]{});
    }


}
