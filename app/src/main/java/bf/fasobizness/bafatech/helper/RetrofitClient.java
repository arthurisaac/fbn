package bf.fasobizness.bafatech.helper;

import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import bf.fasobizness.bafatech.utils.Constants;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static final Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(Constants.HOST_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();

    public static Retrofit getClient() {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        return builder
                .client(okHttpClient)
                .build();

    }

    public static <S> S createService(
            Class<S> serviceClass, String email, String password) {
        if (!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)) {
            String authToken = Credentials.basic(email, password);
            return createService(serviceClass, authToken);
        }

        return createService(serviceClass, null);
    }

    private static <S> S createService(
            Class<S> serviceClass, final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor =
                    new AuthenticationInterceptor(authToken);

            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }

        return retrofit.create(serviceClass);
    }

}
