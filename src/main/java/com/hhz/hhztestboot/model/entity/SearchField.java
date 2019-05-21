package com.hhz.hhztestboot.model.entity;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p class="detail">
 * 功能:查询字段带权重
 * </p>
 *
 * @author HuangHuizhou
 * @ClassName Search field.
 * @Version V1.0.
 * @date 2019.05.20 18:26:57
 */
public class SearchField {
    @ApiModelProperty("查询字段")
    private String field;
    @ApiModelProperty("权重")
    private float weight = 1.0f;

    public SearchField() {
    }

    public SearchField(String field) {
        this(field, 1.0f);
    }

    public SearchField(String field, float weight) {
        this.field = field;
        this.weight = weight;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
