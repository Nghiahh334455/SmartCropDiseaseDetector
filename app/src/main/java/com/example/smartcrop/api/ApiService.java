package com.example.smartcrop.api;

import com.example.smartcrop.models.ChatResponse;
import com.example.smartcrop.models.PredictResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import com.example.smartcrop.models.ChatResponse;
import com.example.smartcrop.models.CommentModel;
import com.example.smartcrop.models.NotificationModel;
import com.example.smartcrop.models.PredictResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
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
    @Multipart
    @POST("/predict")
    Call<PredictResponse> predictDisease(@Part MultipartBody.Part image);

    @POST("/chat")
    Call<ChatResponse> askAI(@Query("question") String question);

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

    @FormUrlEncoded
    @POST("/notifications")
    Call<Map<String, String>> sendNotification(
        @Field("targetUid") String targetUid,
        @Field("senderName") String senderName,
        @Field("senderAvatar") String senderAvatar,
        @Field("type") String type,
        @Field("postId") int postId,
        @Field("postContent") String postContent
    );

    @FormUrlEncoded
    @POST("/users/update_photo")
    Call<Map<String, String>> updateProfilePhoto(@Field("uid") String uid, @Field("photoBase64") String photoBase64);
}
