package fit24.duy.musicplayer.api;

import java.util.List;

import fit24.duy.musicplayer.models.MediaType;
import fit24.duy.musicplayer.models.SearchResponse;
import fit24.duy.musicplayer.models.UserLoginRequest;
import fit24.duy.musicplayer.models.UserRegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    // Gửi OTP
    @FormUrlEncoded
    @POST("api/send-otp")
    Call<Void> sendOtp(@Field("email") String email);

    // Xác thực OTP
    @FormUrlEncoded
    @POST("api/verify-otp")
    Call<Void> verifyOtp(@Field("email") String email,
                         @Field("otp") String otp);

    // Đăng ký user
    @POST("api/register")
    Call<Void> register(@Body UserRegisterRequest request);

    // Đăng nhập
    @POST("api/login")
    Call<Void> login(@Body UserLoginRequest request);

    // Quên mật khẩu
    @FormUrlEncoded
    @POST("api/forgot-password")
    Call<Void> forgotPassword(@Field("email") String email,
                              @Field("newPassword") String newPassword,
                              @Field("otp") String otp);

    @GET("media-types")
    Call<List<MediaType>> getMediaTypes();

    @GET("/api/search")
    Call<SearchResponse> search(@Query("q") String query);
}
