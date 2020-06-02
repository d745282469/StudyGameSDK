package com.dong.studygamesdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dong.mysdk.LoginManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_zfb.setOnClickListener{
            LoginManager.zfbLogin(this,"2021001163636483","_gamesdkdemo_")
        }

        btn_github.setOnClickListener {
            val clientId = "Iv1.fc31c78f5e934054"
            val clientSecret = "46df579cf5c5a9fae4294ead7a66f68e90ad3c97"
            val callbackUrl = "https://www.pandong.site/openGithub/login.html"
            LoginManager.gitHubLogin(clientId,clientSecret,callbackUrl,this)
        }
    }
}
