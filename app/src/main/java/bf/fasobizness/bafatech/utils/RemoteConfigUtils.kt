package bf.fasobizness.bafatech.utils

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

private lateinit var remoteConfig: FirebaseRemoteConfig

object RemoteConfigUtils {
    private const val SERVER_URL = "server_url"
    //private const val LAST_VERSION_OF_APP = "latest_version_of_app"
    private const val MIN_VERSION_OF_APP = "min_version_of_app"
    private const val UPDATE_URL_OF_APP = "update_url_of_app"

    fun init() {
        remoteConfig = getFirebaseRemoteConfig()
    }

    private val DEFAULTS : HashMap<String, Any> = hashMapOf(
            SERVER_URL to "https://fasobizness.com/api/public/",
            //LAST_VERSION_OF_APP to "2.5",
            MIN_VERSION_OF_APP to "2.7",
            UPDATE_URL_OF_APP to "https://play.google.com/store/apps/details?id=bf.fasobizness.bafatech&hl=fr&gl=US"
    )

    private fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {

        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(DEFAULTS)

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            Log.d("activity", "addOnCompleteListener")
        }

        // Log.d("server_url", remoteConfig.getString(SERVER_URL))

        return remoteConfig
    }

    fun getServerUrl(): String = remoteConfig.getString(SERVER_URL)
    fun getMinVersion(): String = remoteConfig.getString(MIN_VERSION_OF_APP)
    //fun getLastVersion(): String = remoteConfig.getString(LAST_VERSION_OF_APP)
    fun getUpdateUrl(): String = remoteConfig.getString(UPDATE_URL_OF_APP)
}