package Gson;

import Renderer.Texture;
import Util.AssetPool;
import com.google.gson.*;

import java.lang.reflect.Type;

public class TextureDeserializer implements JsonDeserializer<Texture> {

    @Override
    public Texture deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String filepath = jsonObject.get("filepath").getAsString();
        return AssetPool.getTexture(filepath);
    }
}
