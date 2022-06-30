package Game;

import org.joml.Vector2f;

public class Transform {

    public Vector2f position;
    public Vector2f scale;

    private float rotation = 0;

    public Transform(){
        init(new Vector2f(), new Vector2f(), 0);
    }

    public Transform(Vector2f position){
        init(position, new Vector2f(), 0);
    }

    public Transform(Vector2f position, Vector2f scale){
        init(position, scale,0);
    }

    public Transform(Vector2f position, Vector2f scale, float rotation){
        init(position, scale,rotation);
    }

    private void init(Vector2f position, Vector2f scale, float rotation) {
        this.position=position;
        this.scale=scale;
        this.rotation=rotation;
    }

    public Transform copy() {
        return new Transform(new Vector2f(this.position), new Vector2f(this.scale), this.rotation);
    }

    public void copy(Transform to) {
        to.position.set(this.position);
        to.scale.set(this.scale);
        to.rotation=this.rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Transform)) return false;

        Transform t = (Transform)o;
        return t.position.equals(this.position) && t.scale.equals(this.scale) && t.rotation==this.rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

}
