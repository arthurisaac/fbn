package bf.fasobizness.bafatech.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import bf.fasobizness.bafatech.MainActivity
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.utils.MySharedManager
import bf.fasobizness.bafatech.utils.RemoteConfigUtils


class ActivitySplash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        RemoteConfigUtils.init()
        val sharedManager: MySharedManager = MySharedManager(this)
        if (sharedManager.serverUrl.isEmpty()) {
            sharedManager.serverUrl = "https://fasobizness.com/api/public/"
        }

        Log.d("server_url", "" + RemoteConfigUtils.getServerUrl())

        val logo: ImageView = findViewById(R.id.logo)
        Handler().postDelayed({
            val intent = Intent(this@ActivitySplash, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 4000)
        val flip = ObjectAnimator.ofFloat(logo, "rotationY", 100f, 0f)
        flip.duration = 1000
        flip.start()
    }
}