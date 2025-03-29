package fit24.duy.musicplayer.api;

import java.util.List;
import fit24.duy.musicplayer.models.MediaType;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("media-types")
    Call<List<MediaType>> getMediaTypes();
}
