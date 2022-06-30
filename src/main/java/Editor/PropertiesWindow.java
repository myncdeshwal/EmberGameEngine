package Editor;

import Components.Gizmo;
import Components.NonPickable;
import Game.Window;
import Physics2D.Components.Box2DCollider;
import Physics2D.Components.CircleCollider;
import Physics2D.Components.Rigidbody2D;
import Util.Settings;
import imgui.ImGui;
import EntityComponent.GameObject;
import Game.MouseListener;
import Renderer.PickingTexture;
import Scenes.Scene;

import java.util.ArrayList;

import static Util.Settings.FPS;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class PropertiesWindow {
    private GameObject activeGameObject = null;
    private PickingTexture pickingTexture = null;
    private int coolDown = 0;

    public PropertiesWindow(PickingTexture pickingTexture) {
        this.pickingTexture = pickingTexture;
    }

    public PropertiesWindow() {
    }

    public void editorUpdate(float dt, Scene currentScene) {
        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && coolDown<=0 && !MouseListener.isDragging()) {
            int x = (int)MouseListener.getScreenX();
            int y = (int)MouseListener.getScreenY();

            if(pointedGameObject(currentScene)!=null){
                    activeGameObject = pointedGameObject(currentScene) ;
            } else
                activeGameObject = null ;

            coolDown=FPS/16;
        }

        if(coolDown>0)
            coolDown--;
    }

    public void imgui() {
        if (activeGameObject != null) {
            ImGui.begin("Properties");
            if (ImGui.beginPopupContextWindow("ComponentAdder")) {
                if (ImGui.menuItem("Add Rigidbody")) {
                    if (activeGameObject.getComponent(Rigidbody2D.class) == null) {
                        activeGameObject.addComponent(new Rigidbody2D());
                    }
                }

                if (ImGui.menuItem("Add Box Collider")) {
                    if (activeGameObject.getComponent(Box2DCollider.class) == null && activeGameObject.getComponent(CircleCollider.class) == null) {
                        activeGameObject.addComponent(new Box2DCollider());
                    }
                }

                if (ImGui.menuItem("Add Circle Collider")) {
                    if (activeGameObject.getComponent(Box2DCollider.class) == null && activeGameObject.getComponent(CircleCollider.class) == null) {
                        activeGameObject.addComponent(new CircleCollider());
                    }
                }
                ImGui.endPopup();
            }
            activeGameObject.imgui();
            ImGui.end();
        }
    }

    public GameObject pointedGameObject(Scene currentScene) {

        //ArrayList<Integer> selected = new ArrayList<>();
        for(int i=0; i<currentScene.gameObjects.size(); i++){
            GameObject go = currentScene.gameObjects.get(i);
            if( Math.abs(MouseListener.getOrthoX()-go.transform.position.x) < go.transform.scale.x/2
                    &&  Math.abs(MouseListener.getOrthoY()-go.transform.position.y) < go.transform.scale.y/2) {
                if(currentScene.gameObjects.get(i).getComponent(NonPickable.class)==null)
                    return currentScene.gameObjects.get(i);
            }
        }

        return null;
    }

    public GameObject getActiveGameObject() {
        return activeGameObject;
    }

    public void setActiveGameObject(GameObject go) {
        activeGameObject = go;
    }


}


