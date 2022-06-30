package Physics2D.Components;


import EntityComponent.Component;
import Renderer.DebugDraw;
import org.jbox2d.common.Vec2;
import org.joml.Vector2f;

public class CircleCollider extends Component {

    private float radius = 1f;
    private Vector2f center = new Vector2f(0,0);
    private boolean bandaid_flag_reset = true;

    protected Vector2f offset = new Vector2f();


    public void setProperties(){
        offset.x = gameObject.transform.scale.x/2;
        offset.y = gameObject.transform.scale.y/2;
        radius = Math.max(gameObject.transform.scale.x/2,gameObject.transform.scale.y/2);

        bandaid_flag_reset = false;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public void editorUpdate(float dt){
        if(gameObject!=null) {
            if (bandaid_flag_reset)
                setProperties();
            center.x = gameObject.transform.position.x;
            center.y = gameObject.transform.position.y;
            DebugDraw.addCircle(center, radius);
        }
    }


    public Vector2f getOffset() {

        return this.offset;
    }

    public void setRadius(float circleRadius) {
        this.radius=circleRadius;
    }

    public void setOffset(Vector2f add) {
        this.offset.add(add);
    }

}
