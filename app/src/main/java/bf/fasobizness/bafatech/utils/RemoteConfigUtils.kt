package bf.fasobizness.bafatech.utils

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigUtils {
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private const val SERVER_URL = "server_url"
    private const val LAST_VERSION_OF_APP = "latest_version_of_app"
    private const val MIN_VERSION_OF_APP = "min_version_of_app"

    fun init() {
        remoteConfig = getFirebaseRemoteConfig()
    }

    private val DEFAULTS : HashMap<String, Any> = hashMapOf(
            SERVER_URL to "https://fasobizness.com/api/public/",
            LAST_VERSION_OF_APP to "2.5",
            MIN_VERSION_OF_APP to "2.4",
    )

    private fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {

        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        // remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.setDefaultsAsync(DEFAULTS)

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            Log.d("activity", "addOnCompleteListener")
        }

        // Log.d("server_url", remoteConfig.getString(SERVER_URL))

        return remoteConfig
    }

    fun getServerUrl(): String = remoteConfig.getString(SERVER_URL)
    fun getMinVersion(): String = remoteConfig.getString(MIN_VERSION_OF_APP)
    fun getLastVersion(): String = remoteConfig.getString(LAST_VERSION_OF_APP)
}