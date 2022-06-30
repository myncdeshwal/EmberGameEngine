package Components;

import Renderer.Texture;
import org.joml.Vector2f;


public class Sprite {

    private Texture texture=null;
    int width, height;

    Vector2f[] texCoords = {
            new Vector2f(1, 1),
            new Vector2f(1, 0),
            new Vector2f(0, 0),
            new Vector2f(0, 1)
    };


    public Texture getTexture() {
        return texture;
    }

    public Vector2f[] getTexCoords() {
        return texCoords;
    }

    public void setTexture(Texture texture){
        this.texture = texture;
    }

    public void setTexCoords(Vector2f[] texCoords){
        this.texCoords = texCoords;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTexId(){
       return texture == null? -1 : texture.getID();
    }
}
