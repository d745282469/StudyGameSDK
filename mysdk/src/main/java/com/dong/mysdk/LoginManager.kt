package com.dong.mysdk

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.alipay.sdk.app.OpenAuthTask
import com.example.basemodlue.HttpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/5/21 15:47
 */
object LoginManager:CoroutineScope {
    private val job = Job() //控制协程
    private val tag = this::class.java.simpleName
    private val zfbCallback = OpenAuthTask.Callback { p0, p1, p2 ->
        Log.d(tag,"支付宝登陆回调，第一个参数：${p0}，第二个参数：${p1}，第三个参数：${bundleToString(p2)}")
    }

    /**
     * 支付宝登陆
     * @param activity 支付宝sdk需要
     * @param appId 用于吊起支付宝sdk，在支付宝的控制中心有
     * @param scheme 用回跳转回来使用的
     * 支付宝回跳到你的应用时使用的 Intent Scheme。请设置为不和其它应用冲突的值。
     * 如果不设置，将无法使用 H5 中间页的方法(OpenAuthTask.execute() 的最后一个参数)回跳至你的应用。
     * 注意！参见 AndroidManifest 中 <AlipayResultActivity> 的 android:scheme，此两处必须设置为相同的值。
     */
    fun zfbLogin(activity:Activity,appId:String,scheme:String){
        // 传递给支付宝应用的业务参数
        val bizParams: MutableMap<String, String> = HashMap()
        bizParams["url"] =
            "https://authweb.alipay.com/auth?auth_type=PURE_OAUTH_SDK&app_id=$appId&scope=auth_user&state=init"

        // 唤起授权业务
        val task = OpenAuthTask(activity)
        task.execute(
            scheme, // Intent Scheme 在AndroidManifest.xml中声明的
            OpenAuthTask.BizType.AccountAuth, // 业务类型
            bizParams, // 业务参数
            zfbCallback, // 业务结果回调。注意：此回调必须被你的应用保持强引用
            true); // 是否需要在用户未安装支付宝 App 时，使用 H5 中间页中转。建议设置为 true。
    }

    private fun bundleToString(bundle: Bundle): String {
        val sb = StringBuilder()
        for (key in bundle.keySet()) {
            sb.append(key).append("=>").append(bundle[key]).append("\n")
        }
        return sb.toString()
    }

    fun gitHubLogin(clientId:String){
        val url = "https://github.com/login/oauth/authorize?client_id=$clientId"
        launch {
            val response = HttpManager.get(url)
            Log.d(tag,"githubLogin：$response")
        }
    }

    override val coroutineContext: CoroutineContext
        get() = job
}