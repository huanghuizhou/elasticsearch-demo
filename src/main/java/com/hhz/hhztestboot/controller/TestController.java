package com.hhz.hhztestboot.controller;

import com.hhz.hhztestboot.dao.UserMapper;
import com.hhz.hhztestboot.model.User;
import com.uniubi.sdk.result.Result;
import com.uniubi.sdk.result.ResultWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p class="detail">
 * 功能:
 * </p>
 *
 * @author HuangHuizhou
 * @ClassName Test controller.
 * @Version V1.0.
 * @date 2019.05.10 16:35:39
 */
@RestController
@Api(value = "test", description = "test - API")
@RequestMapping("/test")
public class TestController {


@Autowired
private UserMapper userMapper;

    @GetMapping("/hhz")
    @ApiOperation(value = "字体测试", notes = "[@黄辉宙]")
    public String  test111() {
        User a=new User();
        a.setAge(15);
        a.setName("asdasd啊");
        userMapper.insert(a);
        return "zasda";

    }

    /**
     * <p class="detail">
     * 功能:
     * </p>
     *
     * @param name :
     * @return result
     * @author huanghuizhou
     * @date 2019.05.10 16:35:39
     */
    @GetMapping
    @ApiOperation(value = "字体测试", notes = "[@黄辉宙]")
    public Result fontTest(@RequestParam String name) {
        return ResultWrapper.wrapSuccess(name);
    }



}
