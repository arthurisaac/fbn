package bf.fasobizness.bafatech.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bf.fasobizness.bafatech.MainActivity
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.annonce.ActivityDetailsAnnonce
import bf.fasobizness.bafatech.helper.MyFirebaseMessagingService
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.MySharedManager
import bf.fasobizness.bafatech.utils.RemoteConfigUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ActivitySplash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        RemoteConfigUtils.init()

        Log.d("server_url", "" + RemoteConfigUtils.getServerUrl())
        val data: Uri? = intent?.data
        val intent: Intent
        if (data != null) {
            val uri: Uri = Uri.parse(data.toString())
            val separated = uri.path.toString().split("/")

            if (separated[1] == "annonce") {
                intent = Intent(this@ActivitySplash, ActivityDetailsAnnonce::class.java)
                intent.putExtra("id_ann", separated[2])
            } else {
                intent = Intent(this@ActivitySplash, MainActivity::class.java)
            }
        } else {
            intent = Intent(this@ActivitySplash, MainActivity::class.java)
        }

        val logo: ImageView = findViewById(R.id.logo)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, 4000)
        val flip = ObjectAnimator.ofFloat(logo, "rotationY", 100f, 0f)
        flip.duration = 1000
        flip.start()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(tag, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            sendRegistrationToServer(token)
        })
    }

    private fun sendRegistrationToServer(token: String) {
        val api = RetrofitClient.getClient().create(API::class.java)
        val sharedManager = MySharedManager(this)
        val user = sharedManager.user
        val auth = sharedManager.token
        if (user.isNotEmpty() && !auth.isEmpty()) {
            val call = api.updateFCM(token, "Bearer $auth")
            call.enqueue(object : Callback<MyResponse?> {
                override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                    Log.d(tag, response.toString())
                }

                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                    Log.d(tag, t.toString())
                }
            })
        }
    }

    companion object {
        private const val tag = "MyFirebaseMessaging"
    }
}