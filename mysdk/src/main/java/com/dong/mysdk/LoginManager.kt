package com.dong.mysdk

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.alipay.sdk.app.OpenAuthTask
import com.dong.mysdk.github.GitHubLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/5/21 15:47
 */
object LoginManager:CoroutineScope {
    private val job = Job() //控制协程
    private val tag = this::class.java.simpleName

    /**
     * 支付宝登陆
     * @param activity 支付宝sdk需要
     * @param appId 用于吊起支付宝sdk，在支付宝的控制中心有
     * @param scheme 用回跳转回来使用的
     * 支付宝回跳到你的应用时使用的 Intent Scheme。请设置为不和其它应用冲突的值。
     * 如果不设置，将无法使用 H5 中间页的方法(OpenAuthTask.execute() 的最后一个参数)回跳至你的应用。
     * 注意！参见 AndroidManifest 中 <AlipayResultActivity> 的 android:scheme，此两处必须设置为相同的值。
     * 官方文档：https://opendocs.alipay.com/open/218/sxc60m
     */
    fun zfbLogin(activity:Activity,appId:String,scheme:String){
        val zfbLogin = ZfbLogin(appId,scheme,activity)
        zfbLogin.login()
    }

    fun gitHubLogin(clientId:String,clientSecret:String,callbackUrl:String,activity: Activity){
        val github = GitHubLogin(
            clientId,
            clientSecret,
            callbackUrl,
            activity
        )
        github.login()
    }

    override val coroutineContext: CoroutineContext
        get() = job
}