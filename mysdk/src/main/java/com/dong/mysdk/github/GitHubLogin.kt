package com.dong.mysdk.github

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dong.mysdk.LoginCallback
import com.dong.mysdk.UserInfo
import com.example.basemodlue.DialogUtil
import com.example.basemodlue.HttpManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.ConnectException
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/5/22 10:43
 */
class GitHubLogin(
    private val clientId: String,
    private val clientSecret: String,
    private val redirectUrl: String,
    private val activity: Activity
) : CoroutineScope {
    private val tag = this::class.java.simpleName
    private val randomStr = UUID.randomUUID().toString()
    private val job = Job()
    private var webView: WebView? = null
    private var hasReplaceContentView = false
    private var dialog: Dialog? = null
    private var callback: LoginCallback? = null

    companion object {
        const val gitHubBaseUrl = "https://github.com/"
        const val gitHubApiUrl = "https://api.github.com/"
    }

    fun login(callback: LoginCallback? = null) {
        this.callback = callback
        initWebView()
        initDialog()
    }

    /**
     * 配置WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView = WebView(activity)
        val param = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView!!.layoutParams = param
        webView!!.settings.javaScriptEnabled = true //允许加载JS
        val url =
            "${gitHubBaseUrl}login/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUrl}" +
                    "&state=${randomStr}"
        webView!!.loadUrl(url)
        webView!!.addJavascriptInterface(this, "android")

        // 如果不设置webviewClient，将会跳转到浏览器
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.d(tag, "webview准备加载：$url")
                view?.loadUrl(url)
                return true
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                Log.d(tag, "webview准备加载：${request?.url.toString()}")
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (!hasReplaceContentView) {
                    // url加载结束，替换掉activity中的contentView
//                    activity.setContentView(view)
                    hasReplaceContentView = true
                }
            }
        }

    }

    /**
     * 使用Dialog的形式去展示WebView
     */
    private fun initDialog() {
        dialog = Dialog(activity)
        dialog!!.setContentView(webView!!)
        dialog!!.window!!.attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog!!.show()
    }

    /**
     * 由js去调用，当登陆回调时，js解析出回调的参数传递过来
     * @param res 由js转换的json字符串
     */
    @JavascriptInterface
    fun loginCallback(res: String) {
        Log.d(tag, "GitHub的js回调：$res")
        val jsonObj = JSONObject(res)
        val code = jsonObj.getString("code")
        val str = jsonObj.getString("state")

        // 校验随机字符串是否相等
        if (str != randomStr) {
            // 不相等表示不是我们的这次请求，所以不管他，相当于失败了
            dialog?.dismiss()
            callback?.onError("请求可能被篡改了！预计值：$randomStr，实际值：$str")
        } else {
            // 现在，去获取access_token
            getAccessToken(code)
        }
    }

    /**
     * 去获取access_token
     *
     * @param code  用户授权后Github回调过来的授权码
     */
    private fun getAccessToken(code: String) {
        val url = "${gitHubBaseUrl}login/oauth/access_token"
        val param = HashMap<String, String>()
        param["client_id"] = clientId
        param["client_secret"] = clientSecret
        param["code"] = code
        param["redirect_uri"] = redirectUrl
        param["state"] = randomStr
        launch(Dispatchers.IO) {
            val result = HttpManager.post(url, param)
            Log.d(tag, "获取access_token结果：$result")
            if (result != null) {
                val resultJsonObj = JSONObject(result.string())
                val token = resultJsonObj.getString("access_token")
                if (token.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        callback?.onError("无法获取access_token：$resultJsonObj")
                    }
                } else {
                    getUserInfo(token)
                }
            } else {
                // 获取access_token失败
                dialog?.dismiss()
                withContext(Dispatchers.Main) {
                    callback?.onError("获取access_token失败，请求${url}时无响应")
                }
            }
        }
    }

    /**
     * 获取用户信息
     *
     * @param token 授权令牌
     */
    private suspend fun getUserInfo(token: String) {
        try {
            val url = "${gitHubApiUrl}user?access_token=$token"
            val result = HttpManager.get(url)

            // 到这里，登陆完成，移除webView
            val json = JSONObject(result!!.string())
            Log.d(tag, "获取用户信息结果：${json}")

            // 可能存在没设置名称，那么就用用户名
            val nickName = if (json.optString("name").isNullOrEmpty()) {
                json.optString("login")
            } else {
                json.optString("name")
            }
            val userInfo = UserInfo(json.optString("id"), nickName, json.toString())
            userInfo.imgUrl = json.optString("avatar_url")
            Log.d(tag, "整理后的用户信息：${userInfo}")
            dialog?.dismiss()
            withContext(Dispatchers.Main) {
                callback?.onSuccess(userInfo)
            }
        } catch (e: ConnectException) {
            // 偶尔会出现连接不上github服务器
            Log.e(tag,"无法连接到Github的服务器")
            dialog?.dismiss()
            withContext(Dispatchers.Main) {
                callback?.onError("无法连接到Github的服务器，请重试")
            }
            e.printStackTrace()
        }
    }

    override val coroutineContext: CoroutineContext
        get() = job
}