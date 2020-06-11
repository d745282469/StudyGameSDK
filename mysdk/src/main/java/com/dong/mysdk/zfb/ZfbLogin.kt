package com.dong.mysdk.zfb

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.alipay.sdk.app.OpenAuthTask
import com.dong.mysdk.LoginCallback
import com.dong.mysdk.UserInfo
import com.example.basemodlue.HttpManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext


/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/5/25 09:18
 * 支付宝登陆
 */
class ZfbLogin(
    private val appId: String,
    private val scheme: String,
    private val privateKey: String?,
    private val activity: Activity
) : CoroutineScope {
    private val job = Job()
    private val tag = this::class.java.simpleName

    private var callback: LoginCallback? = null
    private var authCode: String? = null // 授权成功后才会有
    private val zfbCallback = OpenAuthTask.Callback { code, msg, bundleData ->
        Log.d(tag, "支付宝登陆回调，第一个参数：${code}，第二个参数：${msg}，第三个参数：${bundleToJsonStr(bundleData)}")
        if (code == 9000) {
            authCode = bundleData.getString("auth_code")
            Log.d(tag, "授权成功，authCode:${bundleData.getString("auth_code")}")
            if (privateKey == null) {
                // 不传私钥的话就默认只获取authCode
                val userInfo = UserInfo("", "", bundleToJsonStr(bundleData))
                callback?.onSuccess(userInfo)
            } else {
                // 传了私钥则去获取用户信息
                getAccessToken()
            }
        } else {
            Log.e(tag, "授权失败，错误码：$code。提示信息：$msg")
            callback?.onError("授权失败，错误码：$code。提示信息：$msg")
        }
    }

    /**
     * 名叫login，实际上是向支付宝发起授权请求，获取authCode
     *
     * @param callback 回调函数
     */
    internal fun login(callback: LoginCallback? = null) {
        this.callback = callback
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

    private fun bundleToJsonStr(bundle: Bundle): String {
        val jsonObject = JSONObject()
        for (key in bundle.keySet()) {
            jsonObject.put(key, bundle[key])
        }
        return jsonObject.toString()
    }

    /**
     * 去获取access_token，需要获得授权code
     */
    private fun getAccessToken() {
        // 准备请求参数，获取access_token
        val publicParam = getPublicParam("alipay.system.oauth.token")

        val privateParam = HashMap<String, String>()
        privateParam["grant_type"] = "authorization_code"
        privateParam["code"] = authCode!!

        val formatParamStr = Sign.formatParams(publicParam, privateParam)
        Log.d(tag, "整理后的请求参数:$formatParamStr")

        val sign = Sign.realSign(formatParamStr, privateKey!!)
        Log.d(tag, "签名：$sign")
        publicParam["sign"] = sign!!
        publicParam.putAll(privateParam)

        launch(Dispatchers.IO) {
            // 如果出错的话，看这里https://opendocs.alipay.com/apis/api_9/alipay.system.oauth.token
            val res = HttpManager.post("https://openapi.alipay.com/gateway.do", publicParam)
            val resJson = JSONObject(res!!.string())
            Log.d(tag, "获取access_token结果：$resJson")
            val accessToken = resJson.optJSONObject("alipay_system_oauth_token_response")
                ?.optString("access_token")
            if (accessToken == null || accessToken.isEmpty()) {
                withContext(Dispatchers.Main) {
                    callback?.onError("请求access_token出错了，${resJson}")
                }
                Log.w(
                    tag,
                    "请求access_token出错了，请查看官方文档确认错误原因：https://opendocs.alipay.com/apis/api_9/alipay.system.oauth.token"
                )
            } else {
                // 这里就去获取用户信息了
                getUserInfo(accessToken)
            }
        }
    }

    /**
     * 获取用户信息
     *
     * @param accessToken 授权令牌
     */
    private suspend fun getUserInfo(accessToken: String) {
        val param = getPublicParam("alipay.user.info.share")
        param["auth_token"] = accessToken
        param["sign"] = Sign.realSign(Sign.formatParams(param), privateKey!!)!!
        val infoRes = HttpManager.post("https://openapi.alipay.com/gateway.do", param)
//                Log.d(tag,"获取用户信息：${infoRes?.string()}")
        val infoJson =
            JSONObject(infoRes!!.string()).optJSONObject("alipay_user_info_share_response")
        if (infoJson == null) {
            // 发生错误了
            Log.w(tag, "请求用户信息失败：${infoRes.string()}")
            withContext(Dispatchers.Main) {
                callback?.onError("请求用户信息失败：${infoRes.string()}")
            }
        } else {
            // 完结撒花
            Log.d(tag, "用户信息：$infoJson")
            val userInfo = UserInfo(
                infoJson.optString("user_id"),
                infoJson.optString("nick_name"),
                infoJson.toString()
            )
            userInfo.imgUrl = infoJson.optString("avatar")
            Log.d(tag, "整理后的用户信息：${userInfo}")
            withContext(Dispatchers.Main) {
                callback?.onSuccess(userInfo)
            }
        }
    }

    /**
     * 生成支付宝Http请求的公共参数
     * @param method 每个接口都对应的一个名称
     */
    private fun getPublicParam(method: String): HashMap<String, String> {
        val map = HashMap<String, String>()
        map["app_id"] = appId
        map["method"] = method
        map["charset"] = "utf-8"
        map["sign_type"] = "RSA2"
        map["sign"] = "签名"
        map["timestamp"] =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(System.currentTimeMillis())
        map["version"] = "1.0"
        return map
    }

    override val coroutineContext: CoroutineContext
        get() = job

}