package Renderer;

import Util.Settings;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {
    private Matrix4f projectionMatrix, viewMatrix;
    private Matrix4f inverseProjectionMatrix, inverseViewMatrix;

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void addZoom(float value){
        this.zoom+=value;
    }

    private float zoom=1;
    public Vector2f position;
    public static Vector2f lastPosition = new Vector2f(0,0);


    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.inverseProjectionMatrix = new Matrix4f();
        this.inverseViewMatrix = new Matrix4f();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.identity();
        projectionMatrix.ortho(0.0f, Settings.PROJECTION_WIDTH , 0.0f, Settings.PROJECTION_HEIGHT , 0.0f, 100.0f);
        // height 32 x 34, width 32 x 60
        projectionMatrix.invert(inverseProjectionMatrix);
        lastPosition=position;
    }

    public Matrix4f getViewMatrix() {
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
        this.viewMatrix.identity();
        viewMatrix.lookAt(new Vector3f(position.x, position.y, 20.0f),
                cameraFront.add(position.x, position.y, 0.0f),
                cameraUp);
        this.viewMatrix.invert(inverseViewMatrix);
        return this.viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.ortho(0.0f, Settings.PROJECTION_WIDTH, 0.0f, Settings.PROJECTION_HEIGHT, 0.0f, 100.0f);
        return this.projectionMatrix;
    }

    public Matrix4f getInverseProjectionMatrix(){
        projectionMatrix.invert(inverseProjectionMatrix);
        return this.inverseProjectionMatrix;
    }

    public Matrix4f getInverseViewMatrix() {
        return this.inverseViewMatrix;
    }
}
