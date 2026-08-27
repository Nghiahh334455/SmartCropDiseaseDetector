package com.example.smartcrop.api;

import com.example.smartcrop.models.ChatResponse;
import com.example.smartcrop.models.CommentModel;
import com.example.smartcrop.models.NotificationModel;
import com.example.smartcrop.models.PredictResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // --- USERS ---
    @FormUrlEncoded
    @POST("/users")
    Call<Map<String, String>> syncUser(
        @Field("uid") String uid,
        @Field("displayName") String displayName,
        @Field("email") String email,
        @Field("photoBase64") String photoBase64
    );

    @GET("/users/{uid}")
    Call<Map<String, Object>> getUser(@Path("uid") String uid);

    @Multipart
    @POST("/predict")
    Call<PredictResponse> predictDisease(
        @Part MultipartBody.Part image,
        @Part("user_email") RequestBody email
    );

    @FormUrlEncoded
    @POST("/chat")
    Call<ChatResponse> askAI(@Field("question") String question);

    @FormUrlEncoded
    @POST("/expert-advice")
    Call<Map<String, String>> getExpertAdvice(
        @Field("disease_name_vi") String name,
        @Field("confidence") double confidence
    );

    // --- FORUM ---
    @GET("/posts")
    Call<List<Map<String, Object>>> getPosts();

    @FormUrlEncoded
    @POST("/posts")
    Call<Map<String, String>> createPost(
        @Field("uid") String uid,
        @Field("author") String author,
        @Field("question") String question,
        @Field("userPhotoUrl") String userPhotoUrl,
        @Field("imageUrl") String imageUrl,
        @Field("disease") String disease
    );

    @FormUrlEncoded
    @POST("/likes")
    Call<Map<String, String>> toggleLike(@Field("uid") String uid, @Field("postId") int postId);

    @GET("/likes/{post_id}")
    Call<List<String>> getPostLikes(@Path("post_id") int postId);

    // --- COMMENTS ---
    @GET("/comments/{post_id}")
    Call<List<CommentModel>> getComments(@Path("post_id") int postId);

    @FormUrlEncoded
    @POST("/comments")
    Call<Map<String, String>> addComment(
        @Field("postId") int postId,
        @Field("authorName") String authorName,
        @Field("authorUid") String authorUid,
        @Field("content") String content,
        @Field("authorPhotoUrl") String authorPhotoUrl,
        @Field("parentCommentId") String parentCommentId
    );

    // --- NOTIFICATIONS ---
    @GET("/notifications/{uid}")
    Call<List<NotificationModel>> getNotifications(@Path("uid") String uid);

    @POST("/notifications/read/{notif_id}")
    Call<Map<String, String>> markNotifAsRead(@Path("notif_id") int notifId);

    @GET("/disease_stats/top")
    Call<List<Map<String, Object>>> getTopDiseases();

    @FormUrlEncoded
    @POST("/disease_stats/increment")
    Call<Map<String, String>> incrementDiseaseCount(@Field("name") String name, @Field("imageUrl") String imageUrl);

    // --- DYNAMIC CONTENT (DISEASES & TIPS) ---
    @GET("/diseases")
    Call<List<Map<String, Object>>> getDiseasesFromDb();

    @FormUrlEncoded
    @POST("/diseases")
    Call<Map<String, String>> addDiseaseToDb(
        @Field("name") String name,
        @Field("description") String description,
        @Field("treatment") String treatment,
        @Field("imageResource") String imageResource
    );

    @GET("/tips")
    Call<List<Map<String, Object>>> getTipsFromDb();

    @FormUrlEncoded
    @POST("/tips")
    Call<Map<String, String>> addTipToDb(
        @Field("title") String title,
        @Field("content") String content,
        @Field("imageResource") String imageResource
    );

    // --- ADMIN ---
    @GET("/admin/stats")
    Call<Map<String, Object>> getAdminStats();

    @GET("/admin/users")
    Call<List<Map<String, Object>>> getAdminUsers();

    @retrofit2.http.DELETE("/admin/users/{uid}")
    Call<Map<String, String>> deleteUser(@Path("uid") String uid);

    @retrofit2.http.DELETE("/admin/posts/{post_id}")
    Call<Map<String, String>> deletePostAdmin(@Path("post_id") int postId);

    @retrofit2.http.DELETE("/admin/diseases/{name}")
    Call<Map<String, String>> deleteDiseaseAdmin(@Path("name") String name);

    @retrofit2.http.DELETE("/admin/tips/{tip_id}")
    Call<Map<String, String>> deleteTipAdmin(@Path("tip_id") int tipId);

    @FormUrlEncoded
    @POST("/notifications")
    Call<Map<String, String>> sendNotification(
        @Field("targetUid") String targetUid,
        @Field("senderName") String senderName,
        @Field("senderAvatar") String senderAvatar,
        @Field("senderUid") String senderUid,
        @Field("type") String type,
        @Field("postId") int postId,
        @Field("postContent") String postContent
    );

    @FormUrlEncoded
    @POST("/users/update_photo")
    Call<Map<String, String>> updateProfilePhoto(@Field("uid") String uid, @Field("photoBase64") String photoBase64);

    @GET("/posts/{post_id}")
    Call<Map<String, Object>> getPostById(@Path("post_id") int postId);

    @FormUrlEncoded
    @POST("/history")
    Call<Map<String, String>> saveHistoryToCloud(
        @Field("uid") String uid,
        @Field("diseaseName") String diseaseName,
        @Field("confidence") double confidence,
        @Field("treatment") String treatment,
        @Field("imageBase64") String imageBase64
    );

    @GET("/history/{uid}")
    Call<List<Map<String, Object>>> getHistoryFromCloud(@Path("uid") String uid);
}
