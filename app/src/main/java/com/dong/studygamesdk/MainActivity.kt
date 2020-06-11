package com.dong.studygamesdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dong.mysdk.LoginCallback
import com.dong.mysdk.LoginManager
import com.dong.mysdk.UserInfo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val tag = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_zfb.setOnClickListener {
            val privateKey =
                "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDKyjSQAUYNGFpnAXvObBrjjS1LTCQmrr+ovpF0HC4Mvol1+pjyZbF3C5CVBEO/qO32MStWSTA3yBgShw/dqARXoAbkXMdtcto4B0yVYwHL249fmAYWDHtuUOI/z6RAmQ/J+BUyPo3qcLucw+Vjb7HIdaqqywT5RwHMBl7qPLIUOqUxOKu2auCZ1qQZFwIfQSo8spKvt59fqzjPlKg67EEkvi/6tGa84FpGhNSBzFimQNr/TXHA/Lo/1JAbl1dayyBNW9Z5uWRpRGU818co2Bj09a0/jiH+gz7gy+l4wKwgF59P1YDBHVbRSpRc1ekcGkp+hl7lNnLbPE8OZavRCTErAgMBAAECggEAGKlSjxi4jisiA1jY15eqBXZLxdZypwmlYT4brkUnDIG3cdOR+hiH5nRPlu+31X0Tw1bjcFDiPkZlNwuZ0Fj4fjeF3wuxbQwEob+xfpFVzJbc7sEsLE5ETHwlp6D21qxGkUdMPJoC5DnldwETym3w7vHwWWjrJ+2MuGUl8dKDzZXEyXfbcgb+v/GehBaZM+Gx3QJlXvOGATowmO6yj8m+/2kWiZVP7iab+dFM7r8iFgOyGAHr1RAq2iYn9BZgLqde/hOJACj/7tO0iEmfJola98KlOyxS1In/0ZegCBIsO3q8vzFmfqbK4pOMSACsiz7eNLod/HA9+ZEwKjGva2Z5gQKBgQDmAuUz84r6T8UPBn6M2n0PKHYp2BHzPIdgYjEryA+hV5U4PmkGLtlAeO+V5I16KWkw2GEdN9e11NLFDfGlDadBipLvtpHv3s75Q9zU8hUPdU2sio9sqPTQ+cVKhk780gg86+B9VB6U12rEJ9hfqhGPaBmUvaFSrN0/0zfKDJrM4QKBgQDhs+0qvXbXOg+TedvvDc+tZa9jd5QKKW3ruk7AqCwAMJopb7lfi0/yJxgD1RGd44GkgiUPYYIKckxPMGQZDb+qbBtaJhXJN0dAzYxn1XwhjZvkxJ6rByfjq0y4RImSmbRpdY3qsPwjg5fFjrRI4su4NbMsvx6WhN3Qvp71U29TiwKBgQCajO99jPOhZeA/TFnUQyss+D8NtsW47r74IIJxrKfz/WmitSzUnr4T+C7kRQJbR2ALFhmLDG7BGxPUnJ32DcqI25gHwmElEE16aAMGgvNrw6JBgYkFtjFqCaprfPuuHWKzrLrW+6Lg2C5BR5S4Ewphlc06iIDRkJ3JuR8PgJCzgQKBgQDdsbMSmTmDHMex7y9lt2Y3hrro88rY1Grg8YZrXISjxxmV++TUgW2MiHPhR8dfE9oCsccfPYLTKgPkgL7BkSyjhD61pNUfaQm3EG+Kijq8ZnErGypTCdtXmKnqEYAkHJAGSm6UiTWQ2LAaYBqbwRGjpjORj0THSkjqwmI2zLZ8EwKBgQDg6A3wlTCWJHSjpqnl3xbm7c2Qk6c9kdwXkzCStZHFcEXh4N3VB+JXYafjCpWuXMepuKwVgGnKgVfbdFEzHyU+9i6VqBWEsGbMiiZPUiqyp/M9PoauJE5jEqFqu/em9A+46aKZnd6uIX0po0ifiXNCt7/gfdjGJUARs1cs4FbcMw=="
            LoginManager.zfbLogin(
                this,
                "2021001163636483",
                "_gamesdkdemo_",
                privateKey,
                object : LoginCallback {
                    override fun onSuccess(userInfo: UserInfo) {
                        Log.d(tag, "支付宝登陆成功：$userInfo")
                        this@MainActivity.tv_show.text = userInfo.toString()
                    }

                    override fun onError(msg: String) {
                        Log.e(tag, "支付宝登陆失败：$msg")
                        this@MainActivity.tv_show.text = msg
                    }
                })
        }

        btn_github.setOnClickListener {
            val clientId = "Iv1.fc31c78f5e934054"
            val clientSecret = "46df579cf5c5a9fae4294ead7a66f68e90ad3c97"
            val callbackUrl = "https://www.pandong.site/openGithub/login.html"
            LoginManager.gitHubLogin(
                clientId,
                clientSecret,
                callbackUrl,
                this,
                object : LoginCallback {
                    override fun onSuccess(userInfo: UserInfo) {
                        Log.d(tag, "Github登陆成功：$userInfo")
                        this@MainActivity.tv_show.text = userInfo.toString()
                    }

                    override fun onError(msg: String) {
                        Log.e(tag, "Github登陆失败：$msg")
                        this@MainActivity.tv_show.text = msg

                    }
                })
        }

        btn_wechat.setOnClickListener {
            LoginManager.wechatLogin("wx8f9a81519294482d",this)
        }
    }

    override fun onPause() {
        Log.d(tag, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(tag, "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        super.onDestroy()
    }
}
