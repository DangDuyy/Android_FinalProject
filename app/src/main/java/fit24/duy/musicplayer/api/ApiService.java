package fit24.duy.musicplayer.api;

import java.util.List;

import fit24.duy.musicplayer.models.MediaType;
import fit24.duy.musicplayer.models.SearchResponse;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.models.UserLoginRequest;
import fit24.duy.musicplayer.models.UserRegisterRequest;
import fit24.duy.musicplayer.models.UserResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Gửi OTP
    @FormUrlEncoded
    @POST("send-otp")
    Call<Void> sendOtp(@Field("email") String email);

    // Xác thực OTP
    @FormUrlEncoded
    @POST("verify-otp")
    Call<Void> verifyOtp(@Field("email") String email,
                         @Field("otp") String otp);

    // Đăng ký user
    @POST("register")
    Call<Void> register(@Body UserRegisterRequest request);

    // Đăng nhập
    @POST("login")
    Call<UserResponse> login(@Body UserLoginRequest request);

    // Quên mật khẩu
    @FormUrlEncoded
    @POST("forgot-password")
    Call<Void> forgotPassword(@Field("email") String email,
                              @Field("newPassword") String newPassword,
                              @Field("otp") String otp);

    @GET("media-types")
    Call<List<MediaType>> getMediaTypes();

    @GET("search")
    Call<SearchResponse> search(@Query("q") String query);
    @GET("songs/recently-played")
    Call<List<Song>> getRecentlyPlayed();

    @GET("songs/recommended")
    Call<List<Song>> getRecommended();

    @GET("songs/{id}")
    Call<Song> getSongById(@Path("id") Long id);

    @GET("songs/search")
    Call<List<Song>> searchSongs(@Query("title") String title);

}
