package Components;

import EntityComponent.Component;
import EntityComponent.GameObject;
import EntityComponent.SpriteRenderer;
import Game.KeyListener;
import Game.MouseListener;
import Game.Window;
import Util.Settings;

import static org.lwjgl.glfw.GLFW.*;

public class DragAndDrop extends Component {

    public static boolean safeToSave = true;
    private GameObject holdingObject;
    private Sprite sprite;
    private static float coolDown=0;
    public static float spriteScale = 1;
    private float scaleX;
    private float scaleY;
    public static float scaleCorrection = 1;

    public void pickup(GameObject go) {
        this.safeToSave = false;
        scaleX = go.transform.scale.x;
        scaleY = go.transform.scale.y;
        holdingObject = go;
        go.addComponent(this);
        //Window.getCurrentScene().addGameObjectToScene(go);
    }

    public void pickup(GameObject go, Sprite sprite) {
        this.safeToSave = false;
        scaleX = go.transform.scale.x;
        scaleY = go.transform.scale.y;
        holdingObject = go;
        this.sprite=sprite;
        go.addComponent(this);
        //Window.getCurrentScene().addGameObjectToScene(go);
    }

    public void place() {
        if(spriteScale<=1) {
            holdingObject.transform.position.x = holdingObject.transform.scale.x/2 + (int) (MouseListener.getOrthoX() / holdingObject.transform.scale.x) * holdingObject.transform.scale.x;
            holdingObject.transform.position.y = holdingObject.transform.scale.y/2 + (int) (MouseListener.getOrthoY() / holdingObject.transform.scale.y) * holdingObject.transform.scale.y;
        }
        else {
            holdingObject.transform.position.x = (int) ( ((MouseListener.getOrthoX()-holdingObject.transform.scale.x/4)  / Settings.GRID_WIDTH)) * Settings.GRID_WIDTH;
            holdingObject.transform.position.y = (int) ( ((MouseListener.getOrthoY()-holdingObject.transform.scale.y/4) / Settings.GRID_HEIGHT)) * Settings.GRID_HEIGHT;
        }
        holdingObject.removeComponent(DragAndDrop.class);
        holdingObject = null;
        this.safeToSave = true;

    }

    @Override
    public void editorUpdate(float dt){
        if(holdingObject!=null){


            holdingObject.transform.scale.x = scaleX * spriteScale;
            holdingObject.transform.scale.y = scaleY * spriteScale;

            holdingObject.transform.position.x = MouseListener.getOrthoX();
            holdingObject.transform.position.y = MouseListener.getOrthoY();


            if(MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                place();
            }

            if((MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)|| KeyListener.isKeyPressed(GLFW_KEY_SPACE) )&& coolDown<=0 && false){

                coolDown=Settings.FPS/8;
               try {
                   GameObject go = new GameObject("Sprite_Object_Gen");
                   go.transform.scale.x = Settings.GRID_WIDTH * spriteScale;
                   go.transform.scale.y = Settings.GRID_HEIGHT * spriteScale;

                   if(spriteScale>1) {
                       go.transform.position.x = (int) ( ((MouseListener.getOrthoX()-go.transform.scale.x/4)  / Settings.GRID_WIDTH)) * Settings.GRID_WIDTH;
                       go.transform.position.y = (int) ( ((MouseListener.getOrthoY()-go.transform.scale.y/4) / Settings.GRID_HEIGHT)) * Settings.GRID_HEIGHT;
                   }
                   else {
                       go.transform.position.x = (int) (MouseListener.getOrthoX() / go.transform.scale.x) * go.transform.scale.x;
                       go.transform.position.y = (int) (MouseListener.getOrthoY() / go.transform.scale.y) * go.transform.scale.y;
                   }

                   SpriteRenderer sr = new SpriteRenderer();
                   sr.setSprite(sprite);
                   go.addComponent(sr);
               } catch(Exception e){

               }
            }

            if(coolDown>0)
            coolDown--;
        }
    }

    @Override
    public void update(float dt) {

    }
}

        /*if(test ||KeyListener.isKeyPressed(GLFW_KEY_W) || KeyListener.isKeyPressed(GLFW_KEY_S)){
            int sign=-1;
            if(KeyListener.isKeyPressed(GLFW_KEY_W))
                sign=1;

            GameObject anotherGo;
            anotherGo=new GameObject("Sprite_Object_Gen",
                    new Transform(new Vector2f(holdingObject.transform.position.x,
                            holdingObject.transform.position.y + sign*holdingObject.transform.scale.y),
                            new Vector2f(holdingObject.transform.scale)));
            SpriteRenderer spriteRenderer;
            spriteRenderer = new SpriteRenderer();
            spriteRenderer.setSprite(holdingObject.getComponent(SpriteRenderer.class).getSprite());

            anotherGo.addComponent(spriteRenderer);


            test=false;
            new DragAndDrop().pickup(anotherGo);

        }*/