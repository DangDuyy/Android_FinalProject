package fit24.duy.musicplayer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    @JsonProperty("success") // Ánh xạ với "success" của JSON
    private Boolean status;

    @JsonProperty("message") // Ánh xạ với "message"
    private String message;

    @JsonProperty("data") // Ánh xạ với "data" thay vì "body"
    private T body;

    public Response(Boolean status, String message, T body) {
        this.status = status;
        this.message = message;
        this.body = body;
    }
}
