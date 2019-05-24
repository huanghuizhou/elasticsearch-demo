package com.hhz.hhztestboot.enums;


/**
 * <p class="detail">
 * 功能:搜索类型 0:match 1：matchPhrase 2:term
 * </p>
 *
 * @author huanghuizhou
 * @ClassName Organization state.
 * @Version V1.0.
 * @date 2019.05.09 16:43:37
 */
public enum EsQueryTypeEnum {

    /**
     * 匹配
     */
    match((byte) 0),
    /**
     * 整词匹配
     */
    matchPhrase((byte) 1),

    /**
     * 精准查询
     */
    term((byte) 2),

    ;

    private final byte value;


    EsQueryTypeEnum(byte value) {
        this.value = value;
    }


    /**
     * <p class="detail">
     * 功能:
     * </p>
     *
     * @param value :
     * @return account gender enum
     * @author huanghuizhou
     * @date 2019.05.09 16:43:37
     */
    public static EsQueryTypeEnum valueOf(byte value) {
        for (EsQueryTypeEnum v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid value of EsQuerytypeEnum(%d)", value));
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public byte getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
