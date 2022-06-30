package Renderer;

import EntityComponent.GameObject;
import EntityComponent.SpriteRenderer;
import Game.Window;
import Util.AssetPool;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderBatch implements Comparable<RenderBatch>{


    // Vertex
    // ======
    // Pos               Color                              tex         texID
    // float, float,     float, float, float, float      float, float   float
    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEX_COORDS_SIZE = 2;
    private final int TEX_ID_SIZE=1;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
    private final int VERTEX_SIZE = POS_SIZE + COLOR_SIZE + TEX_COORDS_SIZE + TEX_ID_SIZE;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private float[] isDeleted;

    private int vaoID, vboID;
    private int maxBatchSize;
    private Shader shader;
    private int zIndex;
    Boolean rebuffer = false;

    private List<Texture> textures;
    private int[] texSlots = {0,1,2,3,4,5,6,7};
    public RenderBatch(int maxBatchSize, int zIndex) {
        this.zIndex = zIndex;
        shader=AssetPool.getShader("assets/shaders/default.glsl");
        this.sprites = new SpriteRenderer[maxBatchSize];
        this.maxBatchSize = 10000000;

        // 4 vertices quads
        vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];
        isDeleted = new float[maxBatchSize * 4];
        this.numSprites = 0;
        this.hasRoom = true;
        this.textures = new ArrayList<>();
    }

    public void start() {
        // Generate and bind a Vertex Array Object
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Allocate space for vertices
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Create and upload indices buffer
        int eboID = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Enable the buffer attribute pointers
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);
    }

    public void addSprite(SpriteRenderer spr) {
        // Get index and add renderObject
        rebuffer = true;
        int index = this.numSprites;
        this.sprites[index] = spr;
        this.numSprites++;

        if (spr.getTexture() != null) {
            if (!textures.contains(spr.getTexture()) && textures.size()<5) {
                textures.add(spr.getTexture());
            }
            else if(!textures.contains(spr.getTexture()) && textures.size()<6){
                textures.add(spr.getTexture());
                hasRoom = false;
            }
        }

        // Add properties to local vertices array
        loadVertexProperties(index);

        if (numSprites >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    public void render() {


        for(int i=0; i<numSprites; i++){
            if(sprites[i].getDirtyFlag()) {
                loadVertexProperties(i);
                rebuffer = true;
            }
            if(sprites[i].getDirtyZIndexFlag()){
                destroyIfExists(sprites[i].gameObject);
                sprites[i].setDirtyZIndexFlag(false);
                Window.getCurrentScene().getRenderer().addSafely(sprites[i].gameObject);
            }
        }


        if(rebuffer) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            rebuffer = false;
        }

        // Use shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getCurrentScene().getCamera().getProjectionMatrix());
        shader.uploadMat4f("uView",  Window.getCurrentScene().getCamera().getViewMatrix().scale(1));
        shader.uploadFloat("cameraActive", 1);

        for(int i=0; i<textures.size(); i++){
            glActiveTexture(GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures", texSlots);

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        for(int i=0; i<textures.size(); i++){
            textures.get(i).unbind();
        }

        shader.detach();
    }

    private void clear(int index) {
        int offset = 4*index*VERTEX_SIZE;
        int i;
        for (int counter=0; counter<4; counter++) {
            i=offset+VERTEX_SIZE*counter;

            vertices[i] = 0;
            vertices[i + 1] = 0;
            vertices[i + 2] = 0;
            vertices[i + 3] = 0;
            vertices[i + 4] = 0;
            vertices[i + 5] = 0;
            vertices[i + 6] = 0;
            vertices[i + 7] = 0;
            vertices[i + 8] = 0;
        }
    }


    private void loadVertexProperties(int index) {
        SpriteRenderer sprite = this.sprites[index];

        // Find offset within array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;

        Vector4f color = sprite.getColor();
        Vector2f[] texCoords = sprite.getTexCoords();
        int texID = 0;
        if (sprite.getTexture() != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i) == sprite.getTexture()) {
                    texID = i + 1;
                    break;
                }
            }
        }

        boolean isRotated = sprite.gameObject.transform.getRotation() != 0.0f;
        isRotated = true;
        Matrix4f transformMatrix = new Matrix4f().identity();
        if (isRotated) {
            transformMatrix.translate(sprite.gameObject.transform.position.x + sprite.gameObject.transform.scale.x/2f,
                    sprite.gameObject.transform.position.y - sprite.gameObject.transform.scale.y/2f, 0f);
            transformMatrix.rotate((float)Math.toRadians(sprite.gameObject.transform.getRotation()),
                    0, 0, 1);
            transformMatrix.scale(sprite.gameObject.transform.scale.x,
                    sprite.gameObject.transform.scale.y, 1);
        }

        // Add vertices with the appropriate properties

        float xAdd = 0.5f;
        float yAdd = 0.5f;

        for (int i=0; i < 4; i++) {

            if (i == 1) {
                yAdd = -0.5f;
            } else if (i == 2) {
                xAdd = -0.5f;
            } else if (i == 3) {
                yAdd = 0.5f;
            }


            Vector4f currentPos = new Vector4f(sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x),
                    sprite.gameObject.transform.position.y + (yAdd * sprite.gameObject.transform.scale.y),
                    0, 1);

            if (isRotated) {
                currentPos = new Vector4f(xAdd, yAdd, 0, 1).mul(transformMatrix);
            }

            // Load position

            vertices[offset] = currentPos.x;
            vertices[offset + 1] = currentPos.y;

            //vertices[offset] = sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x);
            //vertices[offset + 1] = sprite.gameObject.transform.position.y + (yAdd * sprite.gameObject.transform.scale.y);

            // Load color
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            // Load texture coordinates
            vertices[offset + 6] = texCoords[i].x;
            vertices[offset + 7] = texCoords[i].y;

            // Load texture id
            vertices[offset + 8] = texID;

            offset += VERTEX_SIZE;

        }
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 per triangle)
        int[] elements = new int[6 * maxBatchSize];
        for (int i=0; i < maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }

        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // 3, 2, 0, 0, 2, 1        7, 6, 4, 4, 6, 5
        // Triangle 1
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset + 0;

        // Triangle 2
        elements[offsetArrayIndex + 3] = offset + 0;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

    public boolean hasRoom() {
        return this.hasRoom;
    }

    public int getZIndex(){
        return zIndex;
    }

    @Override
    public int compareTo(RenderBatch o) {
        return Integer.compare(this.zIndex, o.zIndex);
    }

    public boolean destroyIfExists(GameObject go) {
        SpriteRenderer sprite = go.getComponent(SpriteRenderer.class);
        for (int i=0; i < numSprites; i++) {
            if (sprites[i] == sprite) {
                for (int j=i; j < numSprites - 1; j++) {
                    sprites[j] = sprites[j + 1];
                    sprites[j].setDirtyFlag(true);
                }
                numSprites--;
                return true;
            }
        }

        return false;
    }

}

/*
           if(isDeleted[i]==1)
               continue;
           if(sprites[i].getDeleteFlag()){
                clear(i);
                isDeleted[i]=1;
                rebuffer=true;
            }
            if(sprites[i].getDirtyZIndexFlag()){
                //clear(i);
                //isDeleted[i]=1;
                //rebuffer=true;
                //Window.getCurrentScene().addGameObjectToScene(sprites[i].gameObject);
            }
 */