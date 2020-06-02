package com.dong.mysdk.github

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dong.mysdk.R
import com.dong.mysdk.github.GitHubLogin.Companion.gitHubBaseUrl
import com.example.basemodlue.HttpManager
import kotlinx.android.synthetic.main.activity_github_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.ConnectException
import java.util.*
import kotlin.coroutines.CoroutineContext

class GithubLoginActivity : AppCompatActivity(),CoroutineScope {
    private val tag = this::class.java.simpleName
    private var clientId:String? = null
    private var clientSecret:String? = null
    private var redirectUrl:String? = null
    private val randomStr = UUID.randomUUID().toString()
    private val job = Job() // 控制协程

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_github_login)
        // 获取跳转参数
        clientId = intent.getStringExtra(INTENT_CLIENT_ID)!!
        clientSecret = intent.getStringExtra(INTENT_CLIENT_SECRET)!!
        redirectUrl = intent.getStringExtra(INTENT_REDIRECT_URL)!!


        webview.settings.javaScriptEnabled = true //允许加载JS
        val url = "${gitHubBaseUrl}login/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUrl}" +
                "&state=${randomStr}"
        webview!!.loadUrl(url)
        webview!!.addJavascriptInterface(this, "android")

        // 如果不设置webviewClient，将会跳转到浏览器
        webview!!.webViewClient = object : WebViewClient(){
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
            // 不相等表示不是我们的这次请求，所以不管他，相当于失败了
            Log.e(tag,"非法回调，{$str} != {$randomStr}")
        }else{
            // 现在，去获取access_token
            var url = "${gitHubBaseUrl}login/oauth/access_token"
            val param = HashMap<String,String>()
            param["client_id"] = clientId!!
            param["client_secret"] = clientSecret!!
            param["code"] = code
            param["redirect_uri"] = redirectUrl!!
            param["state"] = randomStr
            launch {
                var result = HttpManager.post(url,param)
                Log.d(tag,"获取access_token结果：$result")
                if (result != null){
                    val resultJsonObj = JSONObject(result)
                    val token = resultJsonObj.getString("access_token")
                    // 去请求获取用户信息，这一步偶尔会出现连接不上github服务器
                    url = "${GitHubLogin.gitHubApiUrl}user?access_token=$token"
                    try {
                        result = HttpManager.get(url)
                        Log.d(tag,"获取用户信息结果：$result")
                        // 到这里，登陆完成
                    }catch (connectException:ConnectException){
                        Log.e(tag,"无法连接到github的服务器，可以多尝试几次，国内经常出现这个问题")
                        connectException.printStackTrace()
                    }
                }
            }
        }
    }

    companion object{
        const val INTENT_CLIENT_ID = "clientId"
        const val INTENT_CLIENT_SECRET = "clientSecret"
        const val INTENT_REDIRECT_URL = "redirectUrl"

        /**
         * 用于跳转到登陆活动
         * @param clientId 来自github注册的app会有的
         * @param clientSecret 来自github注册的app会有的
         * @param redirectUrl  github授权后回调的网页url
         * @param activity 从哪个活动来的
         */
        @JvmStatic
        internal fun start(clientId:String,clientSecret:String,redirectUrl:String,activity:Activity){
            val intent = Intent(activity,GithubLoginActivity::class.java)
            intent.putExtra(INTENT_CLIENT_ID,clientId)
            intent.putExtra(INTENT_CLIENT_SECRET,clientSecret)
            intent.putExtra(INTENT_REDIRECT_URL,redirectUrl)
            activity.startActivity(intent)
        }
    }

    override fun onDestroy() {
        job.cancel() // 当页面销毁时，取消所有的协程
        super.onDestroy()
    }

    override val coroutineContext: CoroutineContext
        get() = job
}
