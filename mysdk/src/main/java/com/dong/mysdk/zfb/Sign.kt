package com.dong.mysdk.zfb

import android.util.Base64
import android.util.Log
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*


/**
 * Create by AndroidStudio
 * Author: pd
 * Time: 2020/6/4 11:01
 * 支付宝参数签名方法，copy from
 * https://github.com/alipay/alipay-easysdk/blob/2fde51bc070c4f6c2b11ab98f357cbe08e7f8a3f/java/src/main/java/com/alipay/easysdk/kernel/util/Signer.java#L54
 */
object Sign {

    /**
     * 对请求参数进行签名前的处理
     * 按照字节码序对每个参数的key的首字母进行排序，剔除key或value为空的参数，剔除sign参数
     *
     * @param publicParam  公共请求参数
     * @param privateParam 部分接口需要的特定请求参数
     */
    fun formatParams(publicParam:Map<String,String>,privateParam:Map<String,String>? = null):String{
        // 对参数进行排序
        val resultMap = TreeMap(publicParam)
        if (privateParam != null) {
            resultMap.putAll(privateParam)
        }
//        resultMap.forEach {
//            Log.d("sign","整理后的key=${it.key},value=${it.value}")
//        }

        // 剔除key或value为空的参数以及sign参数
        val builder = StringBuilder()
        var index = 0
        for (item in resultMap.entries){
            if (!item.key.isNullOrEmpty() && !item.value.isNullOrEmpty() && item.key != "sign"){
//                Log.d("sign","正在整理key=${item.key},value=${item.value}")
                if (index != 0) builder.append("&")
                builder.append(item.key).append("=").append(item.value)
                index++
            }
        }
        return builder.toString()
    }


    /**
     * 计算签名
     *
     * @param content       待签名的内容，经过处理的
     * @param privateKeyPem 私钥
     * @return 签名值的Base64串
     */
    fun realSign(content: String, privateKeyPem: String): String? {
        return try {
            var encodedKey = privateKeyPem.toByteArray()
            encodedKey = Base64.decode(encodedKey,Base64.DEFAULT)
            val privateKey: PrivateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(encodedKey))
            val signature: Signature = Signature.getInstance("SHA256WithRSA")
            signature.initSign(privateKey)
            // default chareset StandardCharsets.UTF_8
            signature.update(content.toByteArray())
            val signed: ByteArray = signature.sign()
            String(Base64.encode(signed,Base64.DEFAULT))
        } catch (e: Exception) {
            val errorMessage =
                "签名遭遇异常，content=" + content + " privateKeySize=" + privateKeyPem.length + " reason=" + e.message
            throw RuntimeException(errorMessage, e)
        }
    }
}