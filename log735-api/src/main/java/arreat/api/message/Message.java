package arreat.api.message;

import com.google.gson.Gson;

public interface Message {

    default String serialize() {
        return new Gson().toJson(this);
    }
}