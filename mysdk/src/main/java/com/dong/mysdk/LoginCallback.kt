package com.dong.mysdk

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/6/11 10:15
 * 登陆回调函数
 */
interface LoginCallback {
    /**
     * 登陆成功
     *
     * @param userInfo  返回用户信息
     */
    fun onSuccess(userInfo: UserInfo)

    /**
     * 登陆失败
     *
     * @param msg   返回错误信息
     */
    fun onError(msg:String)
}