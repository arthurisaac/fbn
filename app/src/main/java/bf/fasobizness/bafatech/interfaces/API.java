package bf.fasobizness.bafatech.interfaces;


import bf.fasobizness.bafatech.models.Advertising;
import bf.fasobizness.bafatech.models.Announce;
import bf.fasobizness.bafatech.models.Discussion;
import bf.fasobizness.bafatech.models.Entreprise;
import bf.fasobizness.bafatech.models.Message;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.models.User;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface API {

    /*
     * User
     */

    @POST("login")
    Call<User> login();

    @GET("v1/users/{id}")
    Call<User> getUser(@Path("id") String id, @Header("Authorization") String auth);

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: PUT"
    })
    @POST("v1/users")
    Call<MyResponse> updateFCM(@Field("token") String token, @Header("Authorization") String auth);

    @PATCH("v1/users/{id}")
    Call<User> updateToken(@Path("id") String id, @Header("Authorization") String auth);

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: PUT"
    })
    @POST("v1/users/{id}")
    Call<User> updateUser(
            @Field("email") String email,
            @Field("username") String username,
            @Field("tel") String tel,
            @Field("nom") String nom,
            @Field("prenom") String prenom,
            @Field("sect_activite") String sect_activite,
            @Path("id") String id,
            @Header("Authorization") String auth);

    /*
     * Announces
     */

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: GET"
    })
    @POST("v1/announces")
    Call<Announce> getAnnounces(@Field("page") String page);

    // Get One Announce
    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: GET"
    })
    @POST("v1/announces/{id}")
    Call<Announce.Annonce> getAnnounce(@Path("id") String id, @Field("user") String user);

    // Search Announces
    @GET("v1/announces/search/{query}")
    Call<Announce> searchAnnounces(@Path("query") String query);

    // Get User's Announces
    @GET("v1/users/announces/{id}")
    Call<Announce> getUsersAnnounces(@Path("id") String id);

    // Create Announce
    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded"
    })
    @POST("v1/announces")
    Call<MyResponse> postAnnounce(
            @Field("id_ann") int id_ann,
            @Field("id_per_fk") String id_per_fk,
            @Field("texte") String texte,
            @Field("prix") String prix,
            @Field("ville") String ville,
            @Field("tel") String tel,
            @Field("tel1") String tel1,
            @Field("tel2") String tel2,
            @Field("titre") String titre,
            @Field("categorie") String categorie
    );

    // Filter Announce
    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded"
    })
    @POST("v1/announces/filter")
    Call<Announce> filterAnnounce(
            @Field("filter") String filter,
            @Field("params") String params
    );


    // Actions
    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: GET"
    })
    @POST("v1/announces/actions/{action}")
    Call<MyResponse> setAnnouncesActions(@Path("action") String id, @Field("id_ann") String id_ann, @Field("user") String user);

    // Favorite
    @GET("v1/favorites")
    Call<Announce> getFavoriteAnnounces(@Header("Authorization") String auth);

    // Delete Announce
    @DELETE("v1/announces/{id}")
    Call<MyResponse> deleteAnnounce(@Path("id") String id);

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: PUT"
    })
    @POST("v1/announces/{id}")
    Call<MyResponse> updateAnnounce(
            @Path("id") String id_ann,
            @Field("id_per_fk") String id_per_fk,
            @Field("texte") String texte,
            @Field("prix") String prix,
            @Field("ville") String ville,
            @Field("tel") String tel,
            @Field("tel1") String tel1,
            @Field("tel2") String tel2,
            @Field("titre") String titre,
            @Field("categorie") String categorie
    );


    /*
     * Ads
     */


    @GET("v1/ads")
    Call<Advertising> getAds();

    @GET("v1/ads/{id}")
    Call<Advertising.Ads> getAd(@Path("id") Integer id);

    @GET("v1/ads/share/{id}")
    Call<MyResponse> shareAd(@Path("id") Integer id);

    /*
     * Message
     */

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: PUT"
    })
    @POST("v1/messages")
    Call<MyResponse> createDiscussion(
            @Field("receiver_id") String receiver_id,
            @Field("user") String user,
            @Field("id_ann") String id_ann
    );


    @GET("v1/messages")
    Call<Discussion> getDiscussions(@Header("Authorization") String auth);


    @PATCH("v1/messages")
    Call<MyResponse> getUnread(@Header("Authorization") String auth);

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: GET"
    })
    @POST("v1/messages/{id}")
    Call<Message> getMessages(
            @Path("id") String id,
            @Field("id_ann") String id_ann,
            @Field("receiver") String receiver,
            @Header("Authorization") String auth);

    @FormUrlEncoded
    @POST("v1/messages")
    Call<Message> createMessages(
            @Field("user_id") String user_id,
            @Field("receiver_id") String receiver_id,
            @Field("message") String message,
            @Field("type") String type,
            @Field("discussion_id") String discussion_id,
            @Header("Authorization") String auth);

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: DELETE"
    })
    @POST("v1/messages")
    Call<MyResponse> deleteDiscussion(
            @Field("id") String id,
            @Header("Authorization") String auth);

    /*
     * Recruits
     */
    @PUT("v1/recruits/{id}")
    Call<Integer> markAsReadRecruit(
            @Path("id") String id
    );

    /*
     * Entreprises
     */

    @FormUrlEncoded
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "X-Http-Method-Override: GET"
    })
    @POST("v1/enterprises")
    Call<Entreprise> getEnterprises(
            @Field("user") String user
    );

    @GET("v1/enterprises/{id}")
    Call<Entreprise.Entreprises> getEnterprise(
            @Path("id") String id
    );

    @FormUrlEncoded
    @POST("v1/comments")
    Call<Entreprise.Entreprises.Comment> createComment(
            @Field("commentaire") String commentaire,
            @Field("user") String user,
            @Field("enterprise") String id
    );

    @DELETE("v1/comments/{id}")
    Call<MyResponse> deleteComment(
            @Path("id") String id
    );

    @FormUrlEncoded
    @POST("v1/likes")
    Call<MyResponse> like(
            @Field("id_articl_fk") String id_articl_fk,
            @Field("id_pers_fk") String user,
            @Header("Authorization") String auth
    );

    @FormUrlEncoded
    @POST("v1/reports")
    Call<MyResponse> createReport(
            @Field("raison") String raison,
            @Field("element") String element,
            @Field("id_element") String id_element
    );

}
