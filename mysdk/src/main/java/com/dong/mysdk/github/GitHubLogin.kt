package com.dong.mysdk.github

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.basemodlue.HttpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
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
    private val redirectUrl:String,
    private val activity: Activity
):CoroutineScope {
    private val tag = this::class.java.simpleName
    private val randomStr = UUID.randomUUID().toString()
    private val job = Job()
    private var webView:WebView? = null
    private var hasReplaceContentView = false

    companion object{
        const val gitHubBaseUrl = "https://github.com/"
        const val gitHubApiUrl = "https://api.github.com/"
    }

    fun login(){
        initWebView()
    }

    private fun initWebView(){
        webView = WebView(activity)
        val param = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView!!.layoutParams = param
        webView!!.settings.javaScriptEnabled = true //允许加载JS
        val url = "${gitHubBaseUrl}login/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUrl}" +
                "&state=${randomStr}"
        webView!!.loadUrl(url)
        webView!!.addJavascriptInterface(this, "android")

        // 如果不设置webviewClient，将会跳转到浏览器
        webView!!.webViewClient = object :WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.d(tag,"webview准备加载：$url")
                view?.loadUrl(url)
                return true
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                Log.d(tag,"webview准备加载：${request?.url.toString()}")
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (!hasReplaceContentView){
                    // url加载结束，替换掉activity中的contentView
                    activity.setContentView(view)
                    hasReplaceContentView = true
                }
            }
        }

    }

    /**
     * 由js去调用，当登陆回调时，js解析出回调的参数传递过来
     * @param res 由js转换的json字符串
     */
    @JavascriptInterface
    fun loginCallback(res:String){
        Log.d(tag,"GitHub的js回调：$res")
        val jsonObj = JSONObject(res)
        val code = jsonObj.getString("code")
        val str = jsonObj.getString("state")

        // 校验随机字符串是否相等
        if (str != randomStr){
            // 不想等表示不是我们的这次请求，所以不管他，相当于失败了
        }else{
            // 现在，去获取access_token
            var url = "${gitHubBaseUrl}login/oauth/access_token"
            val param = HashMap<String,String>()
            param["client_id"] = clientId
            param["client_secret"] = clientSecret
            param["code"] = code
            param["redirect_uri"] = redirectUrl
            param["state"] = randomStr
            launch {
                var result = HttpManager.post(url,param)
                Log.d(tag,"获取access_token结果：$result")
                if (result != null){
                    val resultJsonObj = JSONObject(result)
                    val token = resultJsonObj.getString("access_token")
                    // 去请求获取用户信息，这一步偶尔会出现连接不上github服务器
                    url = "${gitHubApiUrl}user?access_token=$token"
                    result = HttpManager.get(url)
                    Log.d(tag,"获取用户信息结果：$result")
                    // 到这里，登陆完成，移除webView
                }
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = job
}