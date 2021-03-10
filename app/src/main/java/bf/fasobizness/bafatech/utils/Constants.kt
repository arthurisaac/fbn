package bf.fasobizness.bafatech.utils


class Constants {

    companion object HOST {
        fun api_server_url() : String{
            RemoteConfigUtils.init()
             return RemoteConfigUtils.getServerUrl()
        }
    }

    // public static final String HOST_URL = "https://fasobizness.com/api/public/";
    // public static final String HOST_URL = "http://192.168.0.105/api/public/";
    // public static final String HOST_URL = "http://10.0.2.2:8080/";
    // public static final String HOST_URL = "http://192.168.43.236:8000/";
    // public static final String HOST_URL = "http://192.168.2.142:8080/";
}
