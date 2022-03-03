package com.uwallet.pay.main.constant;

import lombok.Getter;

/**
 *
 * @description: 版本控制枚举类
 * @author: aaron
 * @date: Created in 2021-04-07 09:03:29
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: aaronS
 */
@Getter
public enum DataEnum {


    /**
     * 是否是被收藏的商户
     */
    IS_FAVORITE(1,"是收藏的商户"),
    IS_NOT_FAVORITE(0,"不是收藏的商户"),
    /**
     * 商户列表我的收藏数据 要进行的操作
     */
    FAVOURITE_IS_ADD(1,"1:新增我的收藏数据"),
    FAVOURITE_IS_DELETE(0,"1:删除我的收藏数据"),

    /**
     * APP 展示类型
     */
    APP_BANNER_DISPLAY_TYPE_BANNER(1,"app首页banner"),
    APP_DISPLAY_TYPE_EXCLUSIVE(2,"app首页市场推广"),
    APP_BANNER_LIMITS_MAX(9999999,"appBanner展示次数不限制"),

    /**
     * APP跳转类型
     * 1 H5
     * 2 APP
     * 4 CUSTOM
     * 3 NO LINK
     */
    APP_BANNER_REDIRECT_TYPE_H5(1,"H5"),
    APP_BANNER_REDIRECT_TYPE_APP(2,"APP"),
    APP_BANNER_REDIRECT_TYPE_CUSTOM(4,"H5"),


    /**
     * APP banner 上方关闭效果
     * 1 展示
     * 0 不展示
     */
    APP_BANNER_TURNOFF_STATUS_SHOW(1,"show"),

    /**
     * APP banner 上方关闭效果 跳转类型
     * 1 h5
     * 2 app
     * 3 no link
     */
    APP_BANNER_TURNOFF_STATUS_REDIRECT_TYPE_H5(1,"h5"),
    APP_BANNER_TURNOFF_STATUS_REDIRECT_TYPE_APP(2,"app"),
    APP_BANNER_TURNOFF_STATUS_REDIRECT_TYPE_NO(3,"no lin"),


    /**
     * 自定义分类 商户类型
     * 1 自定义
     * 2 和州一样
     */
    CUSTOM_CATEGORY_MERCHANT_STATUS_CUSTOM(1,"自定义"),
    CUSTOM_CATEGORY_MERCHANT_STATUS_SAMEAS(2,"和州一样"),



    /**
     * 分类
     *
     */
    MERCHANT_CATEGORY_CASUAL_DINING(1,"Casual Dining"),
    MERCHANT_CATEGORY_CAFE(2,"Cafe"),
    MERCHANT_CATEGORY_BAR(3,"Bar"),
    MERCHANT_CATEGORY_ASIAN(4,"Asian"),
    MERCHANT_CATEGORY_FAST_CASUAL(5,"Fast Casual"),
    MERCHANT_CATEGORY_EXCLUSIVE_OFFER(6,"Exclusive Offer");

    private final int code;

    private final String message;

    DataEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
