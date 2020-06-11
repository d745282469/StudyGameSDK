package com.dong.mysdk.wechat

import android.content.Context
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.util.*

/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/6/11 11:44
 */
class WechatLogin(private val appId: String, private val context: Context) {
    private var wxApi: IWXAPI? = null

    internal fun login() {
        wxApi = WXAPIFactory.createWXAPI(context,appId)
        wxApi!!.registerApp(appId)

        val request = SendAuth.Req()
        request.scope = "snsapi_userinfo"
        request.state = UUID.randomUUID().toString() // 防止跨域攻击，可以不传
        wxApi!!.sendReq(request)
    }
}