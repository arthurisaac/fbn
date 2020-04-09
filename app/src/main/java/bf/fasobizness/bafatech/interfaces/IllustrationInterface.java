package bf.fasobizness.bafatech.interfaces;

import java.util.List;

import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.MyResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IllustrationInterface {

    @Multipart
    @POST("v1/illustration")
    Call<ResponseBody> uploadPhotos(
            @Part("id") RequestBody id_annonce_fk,
            @Part("size") RequestBody size,
            @Part List<MultipartBody.Part> files
    );

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: GET"
    })
    @POST("v1/illustration")
    Call<Announce.Annonce> getIllustrations(@Field("id") String id);

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: DELETE"
    })
    @POST("v1/illustration")
    Call<MyResponse> deleteIllustrations(
            @Field("id") String id
    );
}
