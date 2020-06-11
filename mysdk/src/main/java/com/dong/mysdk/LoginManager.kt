package com.dong.mysdk

import android.app.Activity
import android.content.Context
import com.dong.mysdk.github.GitHubLogin
import com.dong.mysdk.wechat.WechatLogin
import com.dong.mysdk.zfb.ZfbLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.security.PrivateKey
import kotlin.coroutines.CoroutineContext


/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/5/21 15:47
 */
object LoginManager {
    private val tag = this::class.java.simpleName

    /**
     * 支付宝登陆
     *
     * @param activity   支付宝sdk需要
     * @param appId      用于吊起支付宝sdk，在支付宝的控制中心有
     * @param scheme     用回跳转回来使用的
     * @param privateKey 应用私钥，强烈不建议在客户端做签名，有私钥暴露的风险！！！
     * @param callback   回调函数
     *
     * 支付宝回跳到你的应用时使用的 Intent Scheme。请设置为不和其它应用冲突的值。
     * 如果不设置，将无法使用 H5 中间页的方法(OpenAuthTask.execute() 的最后一个参数)回跳至你的应用。
     * 注意！参见 AndroidManifest 中 <AlipayResultActivity> 的 android:scheme，此两处必须设置为相同的值。
     * 官方文档：https://opendocs.alipay.com/open/218/sxc60m
     */
    fun zfbLogin(
        activity: Activity,
        appId: String,
        scheme: String,
        privateKey: String? = null,
        callback: LoginCallback? = null
    ) {
        val zfbLogin = ZfbLogin(appId, scheme, privateKey, activity)
        zfbLogin.login(callback)
    }

    /**
     * github登陆
     * 使用webview的方式进行授权
     *
     * @param clientId     类似与AppId
     * @param clientSecret 类似于AppSecret
     * @param callbackUrl  github会将授权码回调到这个url上
     * @param activity     用户展示Dialog和提供Context的
     * @param callback     回调函数
     */
    fun gitHubLogin(
        clientId: String,
        clientSecret: String,
        callbackUrl: String,
        activity: Activity,
        callback: LoginCallback? = null
    ) {
        val github = GitHubLogin(
            clientId,
            clientSecret,
            callbackUrl,
            activity
        )
        github.login(callback)
    }

    fun wechatLogin(appId: String,context:Context){
        val wechat = WechatLogin(appId,context)
        wechat.login()
    }
}