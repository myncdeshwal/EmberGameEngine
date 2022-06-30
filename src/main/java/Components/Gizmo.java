package Components;

import Editor.PropertiesWindow;
import EntityComponent.Component;
import EntityComponent.GameObject;
import EntityComponent.SpriteRenderer;
import Game.KeyListener;
import Game.MouseListener;
import Util.Settings;
import Util.TransitionDnD;
import Game.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class Gizmo extends Component {
    private Vector4f xAxisColor = new Vector4f(1, 0.3f, 0.3f, 1);
    private Vector4f xAxisColorHover = new Vector4f(1, 0, 0, 1);
    private Vector4f yAxisColor = new Vector4f(0.3f, 1, 0.3f, 1);
    private Vector4f yAxisColorHover = new Vector4f(0, 1, 0, 1);

    private GameObject xAxisObject;
    private GameObject yAxisObject;
    private SpriteRenderer xAxisSprite;
    private SpriteRenderer yAxisSprite;


    protected GameObject activeGameObject = null;

    private Vector2f xAxisOffset = new Vector2f(Settings.GRID_WIDTH, -1.3f*Settings.GRID_HEIGHT);
    private Vector2f yAxisOffset = new Vector2f(-.3f*Settings.GRID_WIDTH, 0);

    private float gizmoWidth = 16/160f;
    private float gizmoHeight = 48f/ 160f;

    protected boolean xAxisActive = false;
    protected boolean yAxisActive = false;

    private boolean using = false;

    private PropertiesWindow propertiesWindow;

    public Gizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
        this.xAxisObject = TransitionDnD.generateSpriteObject(arrowSprite, gizmoWidth, gizmoHeight, 5);
        this.yAxisObject = TransitionDnD.generateSpriteObject(arrowSprite, gizmoWidth, gizmoHeight,5 );
        this.xAxisSprite = this.xAxisObject.getComponent(SpriteRenderer.class);
        this.yAxisSprite = this.yAxisObject.getComponent(SpriteRenderer.class);
        this.propertiesWindow = propertiesWindow;

        this.xAxisObject.addComponent(new NonPickable());
        this.yAxisObject.addComponent(new NonPickable());

        Window.getCurrentScene().addGameObjectToScene(this.xAxisObject);
        Window.getCurrentScene().addGameObjectToScene(this.yAxisObject);
    }

    @Override
    public void start() {
        this.xAxisObject.transform.setRotation(90);
        this.yAxisObject.transform.setRotation(180);
        this.xAxisObject.setNoSerialize();
        this.yAxisObject.setNoSerialize();
        shouldImgui = false;
    }

    @Override
    public void editorUpdate(float dt) {
        if (!using) return;

        this.activeGameObject = this.propertiesWindow.getActiveGameObject();
        if (this.activeGameObject != null) {
            this.setActive();


            // TODO: move this into it's own keyEditorBinding component class
            if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) &&
                    KeyListener.keyBeginPress(GLFW_KEY_V)) {
                GameObject newObj = this.activeGameObject.copy();
                Window.getCurrentScene().addGameObjectToScene(newObj);
                newObj.transform.position.x = newObj.transform.scale.x/2 + (int) (MouseListener.getOrthoX() / newObj.transform.scale.x) * newObj.transform.scale.x;
                newObj.transform.position.y = newObj.transform.scale.y/2 +(int) (MouseListener.getOrthoY() / newObj.transform.scale.y) * newObj.transform.scale.y;
                this.propertiesWindow.setActiveGameObject(newObj);
                return;
            } else if (KeyListener.keyBeginPress(GLFW_KEY_DELETE)) {
                activeGameObject.destroy();
                this.setInactive();
                this.propertiesWindow.setActiveGameObject(null);
                return;
            }

        } else {
            this.setInactive();
            return;
        }

        boolean xAxisHot = checkXHoverState();
        boolean yAxisHot = checkYHoverState();

        if ((xAxisHot || xAxisActive)  && MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            xAxisActive = true;
            yAxisActive = false;
        } else if ((yAxisHot || yAxisActive)  && MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            yAxisActive = true;
            xAxisActive = false;
        } else {
            xAxisActive = false;
            yAxisActive = false;
        }

        if (this.activeGameObject != null) {
            this.xAxisObject.transform.position.set(this.activeGameObject.transform.position);
            this.yAxisObject.transform.position.set(this.activeGameObject.transform.position);

            float scalingX =this.activeGameObject.transform.scale.x;
            float scalingY =this.activeGameObject.transform.scale.y;

            this.yAxisObject.transform.scale.x= Math.min(scalingX,scalingY)/48f * 16f;
            this.yAxisObject.transform.scale.y= scalingY;

            this.xAxisObject.transform.scale.x= Math.min(scalingX,scalingY)/48f * 16f;
            this.xAxisObject.transform.scale.y= scalingX;

            xAxisOffset.set(this.activeGameObject.transform.scale.x/2.2f, this.activeGameObject.transform.scale.y/2.2f);
            yAxisOffset.set(this.activeGameObject.transform.scale.x/2.2f, this.activeGameObject.transform.scale.y/2.2f);

            this.yAxisObject.transform.position.x = this.yAxisObject.transform.position.x - xAxisOffset.x;
            this.xAxisObject.transform.position.y = this.yAxisObject.transform.position.y - yAxisOffset.y;

        }
    }


    private void setActive() {
        this.xAxisSprite.setColor(xAxisColor);
        this.yAxisSprite.setColor(yAxisColor);
    }

    private void setInactive() {
        this.activeGameObject = null;
        this.xAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
        this.yAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
    }

    private boolean checkXHoverState() {
        if( Math.abs(MouseListener.getOrthoX()-xAxisObject.transform.position.x) < xAxisObject.transform.scale.y/2
                &&  Math.abs(MouseListener.getOrthoY()-xAxisObject.transform.position.y) < xAxisObject.transform.scale.x/2) {
            xAxisSprite.setColor(xAxisColorHover);
            return true;
        }

        xAxisSprite.setColor(xAxisColor);
        return false;
    }

    private boolean checkYHoverState() {

        if( Math.abs(MouseListener.getOrthoX()-yAxisObject.transform.position.x) < yAxisObject.transform.scale.x/2
                &&  Math.abs(MouseListener.getOrthoY()-yAxisObject.transform.position.y) < yAxisObject.transform.scale.y/2) {
            yAxisSprite.setColor(yAxisColorHover);
            return true;
        }

        yAxisSprite.setColor(yAxisColor);
        return false;
    }

    public void setUsing() {
        this.using = true;
    }

    public void setNotUsing() {
        this.using = false;
        this.setInactive();
    }

    public void setActiveGameObject(GameObject activeGameObject) {
        this.activeGameObject = activeGameObject;
    }

    @Override
    public void update(float dt){
        if(Window.isRunTimePlaying() && using)
            setNotUsing();
    }


}