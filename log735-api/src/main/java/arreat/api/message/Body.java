package arreat.api.message;

import com.google.gson.Gson;

public interface Body {

    default String toJson() {
        return new Gson().toJson(this);
    }
}
