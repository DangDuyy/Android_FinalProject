package fit24.duy.musicplayer.api;

import java.util.List;
import fit24.duy.musicplayer.models.MediaType;
import fit24.duy.musicplayer.models.SearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("media-types")
    Call<List<MediaType>> getMediaTypes();

    @GET("/api/search")
    Call<SearchResponse> search(@Query("q") String query);
}
