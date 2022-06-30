package EntityComponent;

import Components.Sprite;
import Editor.MyImGui;
import Game.Transform;
import Renderer.Texture;
import org.joml.Vector2f;
import org.joml.Vector4f;


public class SpriteRenderer extends Component {

    private transient boolean firstTime = true;
    private Vector4f color = new Vector4f(1,1,1,1);
    private Sprite sprite = new Sprite();
    private transient Transform lastTransform;
    private transient Boolean dirtyFlag = true;
    private transient boolean deleteFlag = false;
    private transient boolean dirtyZIndexFlag = false;

    public boolean getDirtyZIndexFlag() {
        return dirtyZIndexFlag;
    }


    public Vector4f getColor(){
        return this.color;
    }

    @Override
    public void start() {
        firstTime = false;
        lastTransform = gameObject.transform.copy();
    }

    @Override
    public void update(float dt) {
        if (firstTime) {                                //done so component could be added at runtime
            start();
        }  else {
                if (!this.lastTransform.equals(this.gameObject.transform)) {
                    this.gameObject.transform.copy(this.lastTransform);
                    setDirtyFlag(true);
                }
            }
        }

        @Override
    public void imgui() {
        Vector4f temp =new Vector4f(color.x, color.y, color.z, color.w);
        if ( MyImGui.colorPicker4("Color Picker: ",temp) ) {
            this.setColor(temp);
        }
    }

    public Texture getTexture(){
        return sprite.getTexture();
    }

    public Vector2f[] getTexCoords(){
        return sprite.getTexCoords();
    }

    public boolean getDirtyFlag() {
        return dirtyFlag;
    }

    public void setDirtyFlag(Boolean dirtyFlag){
        this.dirtyFlag = dirtyFlag;
    }

    public void setColor(Vector4f color){
        if(!this.color.equals(color)){
            this.color = color;
            if(!firstTime){
                setDirtyFlag(true);
            }
        }
    }

    public void setSprite(Sprite sprite){
        this.sprite = sprite;
        if(!firstTime)setDirtyFlag(true);

    }

    public void clean(){
        setDirtyFlag(false);
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag=deleteFlag;
    }

    public boolean getDeleteFlag() {
        return deleteFlag;
    }

    @Override
    public void destroy(){
        setDeleteFlag(true);
    }

    @Override
    public void editorUpdate(float dt) {
        if (firstTime) {                                //done so component could be added at runtime
            start();
        }  else {
            if (!this.lastTransform.equals(this.gameObject.transform)) {
                this.gameObject.transform.copy(this.lastTransform);
                setDirtyFlag(true);
            }
        }
    }

    public Sprite getSprite(){
        return sprite;
    }

    public void setDirtyZIndexFlag(boolean b) {
        dirtyZIndexFlag = b;
    }

    public void setTexture(Texture texture) {
        sprite.setTexture(texture);
    }

}