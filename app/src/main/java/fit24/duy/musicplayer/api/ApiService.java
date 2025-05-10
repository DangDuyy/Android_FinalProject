package fit24.duy.musicplayer.api;

import java.util.List;

import fit24.duy.musicplayer.models.Album;
import fit24.duy.musicplayer.models.ApiResponse;
import fit24.duy.musicplayer.models.Artist;
import fit24.duy.musicplayer.models.MediaType;
import fit24.duy.musicplayer.models.MediaTypeResponse;
import fit24.duy.musicplayer.models.SearchResponse;
import fit24.duy.musicplayer.models.Song;
import fit24.duy.musicplayer.models.UserLoginRequest;
import fit24.duy.musicplayer.models.UserRegisterRequest;
import fit24.duy.musicplayer.models.UserResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @GET("media-types/{mediaTypeId}")
    Call<List<MediaTypeResponse>> getSongsByMediaType(@Path("mediaTypeId") Long mediaTypeId);

    @GET("search")
    Call<SearchResponse> search(@Query("q") String query);

    @GET("songs/artist")
    Call<List<Song>> getSongsByArtist(@Query("name") String artistName);

    @GET("songs/album")
    Call<List<Song>> getSongsByAlbum(@Query("title") String albumTitle);

    @POST("artists/{artistId}/follow")
    Call<ApiResponse<String>> followArtist(@Path("artistId") Long artistId, @Query("userId") Long userId);

    @DELETE("artists/{artistId}/unfollow")
    Call<ApiResponse<String>> unfollowArtist(@Path("artistId") Long artistId, @Query("userId") Long userId);

    @GET("artists/{artistId}/is-followed")
    Call<ApiResponse<Boolean>> isArtistFollowed(@Path("artistId") Long artistId, @Query("userId") Long userId);

    @POST("albums/{albumId}/add-to-library")
    Call<ApiResponse<String>> addAlbumToLibrary(@Path("albumId") Long albumId, @Query("userId") Long userId);

    @DELETE("albums/{albumId}/remove-from-library")
    Call<ApiResponse<String>> removeAlbumFromLibrary(@Path("albumId") Long albumId, @Query("userId") Long userId);

    @GET("albums/{albumId}/is-in-library")
    Call<ApiResponse<Boolean>> isAlbumInLibrary(@Path("albumId") Long albumId, @Query("userId") Long userId);

    @GET("artists/followed_artists/{userId}")
    Call<List<Artist>> getFollowedArtists(@Path("userId") Long userId);

    @GET("albums/library/{userId}")
    Call<List<Album>> getLibraryAlbums(@Path("userId") Long userId);
    @GET("songs/recently-played")
    Call<List<Song>> getRecentlyPlayed();

    @GET("songs/recommended")
    Call<List<Song>> getRecommended();

    @GET("songs/{id}")
    Call<Song> getSongById(@Path("id") Long id);

    @GET("songs/search")
    Call<List<Song>> searchSongs(@Query("title") String title);

    @GET("/api/songs/random")
    Call<List<Song>> getRandomSongs(@Query("count") int count);

    @GET("/api/{id}/lyrics")
    Call<String> getLyrics(@Path("id") Long id);

}
