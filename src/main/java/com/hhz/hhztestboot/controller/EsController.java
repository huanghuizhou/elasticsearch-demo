package com.hhz.hhztestboot.controller;

import com.hhz.hhztestboot.model.Account;
import com.hhz.hhztestboot.model.MedusaEsPage;
import com.hhz.hhztestboot.model.entity.SearchField;
import com.hhz.hhztestboot.util.ElasticsearchUtil;
import com.uniubi.sdk.util.UuidUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <p class="detail">
 * 功能:
 * </p>
 *
 * @author HuangHuizhou
 * @ClassName Es controller.
 * @Version V1.0.
 * @date 2019.05.18 17:39:14
 */
@RestController
@Api(value = "es", description = "es - API")
@RequestMapping("/es")
public class EsController {

    /**
     * 测试索引
     */
    @Value("${elasticsearch.index}")
    private String indexName ;

    /**
     * 类型
     */
    private String esType = "account";

    /**
     * 创建索引
     *
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 17:39:14
     */
    @PostMapping("/index")
    @ApiOperation(value = "创建index", notes = "[@黄辉宙]")
    public String createIndex() {
        if (ElasticsearchUtil.isIndexExist(indexName)) {
            return "索引已经存在";
        }
        if(ElasticsearchUtil.createIndex(indexName)){
            return "索引创建成功";
        }else {
            return "索引创建失败";
        }
    }

    /**
     * <p class="detail">
     * 功能:删除索引
     * </p>
     *
     * @return string
     * @author huanghuizhou
     * @date 2019.05.21 09:55:18
     */
    @DeleteMapping("/index")
    @ApiOperation(value = "删除index", notes = "[@黄辉宙]")
    public String deleteIndex() {
        if (!ElasticsearchUtil.isIndexExist(indexName)) {
            return "索引不存在";
        }
        if(ElasticsearchUtil.deleteIndex(indexName)){
            return "索引删除成功";
        }else {
            return "索引删除失败";
        }
    }

    /**
     * 插入记录
     *
     * @param account :
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 17:39:14
     */
    @PostMapping("/account")
    @ApiOperation(value = "新增员工", notes = "[@黄辉宙]")
    public String insertJson(Account account) {
        account.setDate(new Date());
        String uuid=UuidUtil.uuid();
        account.setId(uuid);
        return ElasticsearchUtil.addData(account, indexName, esType);
    }

    /**
     * 删除记录
     *
     * @param id :
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 17:39:14
     */
    @DeleteMapping("/account")
    @ApiOperation(value = "删除account", notes = "[@黄辉宙]")
    public String delete(@RequestParam(value = "id") String id) {
        if (StringUtils.isNotBlank(id)) {
            ElasticsearchUtil.deleteDataById(indexName, esType, id);
            return "删除id=" + id;
        } else {
            return "id为空";
        }
    }

    /**
     * 更新数据
     *
     * @param id      :
     * @param account :
     * @return
     * @author huanghuizhou
     * @date 2019.05.18 17:39:14
     */
    @PutMapping("/account/{id}")
    @ApiOperation(value = "修改account", notes = "[@黄辉宙]")
    public String update(@PathVariable  String id,@RequestBody Account account) {
        account.setId(id);
        if (StringUtils.isNotBlank(id)) {
            ElasticsearchUtil.updateDataById(account, indexName, esType);
            return "修改id=" + id;
        } else {
            return "id为空";
        }
    }

    /**
     * 获取数据
     *
     * @param id the id
     * @return data data
     */
    @GetMapping("/account/{id}")
    @ApiOperation(value = "获取account", notes = "[@黄辉宙]")
    public Account getData(@PathVariable String id) {
        return  ElasticsearchUtil.searchDataById(indexName, esType, id, Account.class);
    }


    /**
     * 查询数据
     * 模糊查询
     *
     * @param keyWord     :
     * @param startPage   :
     * @param pageSize    :
     * @param matchFields :
     * @return es page
     * @author huanghuizhou
     * @date 2019.05.18 17:39:14
     */
    @GetMapping("/queryMatch")
    @ApiOperation(value = "匹配查询（权重都为1）", notes = "[@黄辉宙]")
    public MedusaEsPage<Account> queryMatch(@RequestParam @ApiParam(value = "查询内容") String keyWord,@RequestParam @ApiParam(value = "是否高亮") boolean highShow,@RequestParam(defaultValue = "0") @ApiParam("起始页") Integer startPage,@RequestParam(defaultValue = "10") @ApiParam("每页个数") Integer pageSize,String [] matchFields) {

        MedusaEsPage<Account> accountMedusaEsPage = ElasticsearchUtil.
                searchEs(indexName, esType, startPage, pageSize, keyWord, null, Account.class,highShow,matchFields);
        return accountMedusaEsPage;
    }

    /**
     * 查询数据
     * 模糊查询
     *
     * @param keyWord     :
     * @param startPage   :
     * @param pageSize    :
     * @param matchFields :
     * @return es page
     * @author huanghuizhou
     * @date 2019.05.18 17:39:14
     */
    @PostMapping("/queryMatchWithWeight")
    @ApiOperation(value = "匹配查询（自定义权重）", notes = "[@黄辉宙]")
    public MedusaEsPage<Account> queryMatchWithWeight(@RequestParam @ApiParam(value = "查询内容") String keyWord,@RequestParam @ApiParam(value = "是否高亮") boolean highShow,@RequestParam(defaultValue = "0") @ApiParam Integer startPage,@RequestParam(defaultValue = "10") @ApiParam Integer pageSize, @RequestBody List<SearchField> matchFields) {
        MedusaEsPage<Account> accountMedusaEsPage = ElasticsearchUtil.
                searchEs(indexName, esType, startPage, pageSize, keyWord, null, Account.class,highShow,matchFields.toArray(new SearchField[]{}));
        return accountMedusaEsPage;
    }


}
