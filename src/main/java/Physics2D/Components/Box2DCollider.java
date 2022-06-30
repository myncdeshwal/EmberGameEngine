package Physics2D.Components;

import Components.Sprite;
import EntityComponent.GameObject;
import EntityComponent.SpriteRenderer;
import Game.Window;
import Renderer.DebugDraw;
import EntityComponent.Component;
import Util.AssetPool;
import Util.Settings;
import Util.TransitionDnD;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Box2DCollider extends Component {
    private Vector2f halfSize = new Vector2f(0.5f);
    private Vector2f origin = new Vector2f();
    private boolean resetBox = true;
    private Vector2f offset = new Vector2f();


    public Vector2f getHalfSize() {
        return halfSize;
    }

    public void setHalfSize(Vector2f halfSize) {
        this.halfSize = halfSize;
    }

    public Vector2f getOrigin() {
        return this.origin;
    }


    @Override
    public void editorUpdate(float dt) {
        if(gameObject!=null) {
        if (!Window.isRunTimePlaying()) {
            if (resetBox) {
                setHalfSize();
                setOrigin();
                resetBox = false;
            }
        }

        float XscaleBy2 = halfSize.x * 2;
        float YscaleBy2 = halfSize.y * 2;
        DebugDraw.addBox2D(new Vector2f(gameObject.transform.position.x, gameObject.transform.position.y),
                new Vector2f(XscaleBy2, YscaleBy2), gameObject.transform.getRotation(), new Vector3f(1, 0, 0), 1);
        }

    }

    private void setOrigin() {
        origin.x= 0;
        origin.y= 0;
    }

    @Override
    public void update(float dt){
        //editorUpdate(dt);
    }


    public Vector2f getOffset() {

        return this.offset;
    }
    public void setOffset(Vector2f add) {
        this.offset.add(add);
    }

    public void setHalfSize(){
        halfSize.x = gameObject.transform.scale.x;
        halfSize.y = gameObject.transform.scale.y;


        float hs_cofficientX = .5f;
        float hs_cofficientY = .5f;

        halfSize.x *= hs_cofficientX;
        halfSize.y *= hs_cofficientX;
    }

}
