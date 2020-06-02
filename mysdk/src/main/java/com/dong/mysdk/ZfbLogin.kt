package com.dong.mysdk

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.alipay.sdk.app.OpenAuthTask

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/5/25 09:18
 * 支付宝登陆
 */
class ZfbLogin(
    private val appId: String,
    private val scheme: String,
    private val activity: Activity
) {
    private val tag = this::class.java.simpleName
    private val zfbCallback = OpenAuthTask.Callback { p0, p1, p2 ->
        Log.d(tag, "支付宝登陆回调，第一个参数：${p0}，第二个参数：${p1}，第三个参数：${bundleToString(p2)}")
    }

    internal fun login() {
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
            true // 是否需要在用户未安装支付宝 App 时，使用 H5 中间页中转。
        )
    }

    private fun bundleToString(bundle: Bundle): String {
        val sb = StringBuilder()
        for (key in bundle.keySet()) {
            sb.append(key).append("=>").append(bundle[key]).append("\n")
        }
        return sb.toString()
    }
}