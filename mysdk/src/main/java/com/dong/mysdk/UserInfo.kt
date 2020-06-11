package com.dong.mysdk

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/6/4 15:43
 * 用户信息实体类
 *
 * @param uid           每个用户体系都会有自己的uid，用于区分用户
 * @param nickName      用户昵称
 * @param originalData  原始数据，每个登陆平台都不同
 * @property imgUrl     有些平台会提供用户的头像地址，有些没有
 */
data class UserInfo(val uid:String,val nickName:String,val originalData:String) {
    var imgUrl:String? = null
}