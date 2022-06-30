package EntityComponent;

import Editor.MyImGui;
import Game.ImGuiLayer;
import Game.Transform;
import Game.Window;
import Gson.ComponentDeserializer;
import Gson.GameObjectDeserializer;
import Util.AssetPool;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import imgui.ImGui;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class GameObject {

    private static final int DEFAULT_Z_INDEX = 0;
    public String name;
    private List<Component> components;
    public  Transform transform;
    private int zIndex;
    private static int ID_COUNTER = 0;
    private int uid = -1;
    private transient boolean  doSerialize = true;
    private transient boolean isDead = false;

    // CONSTRUCTS POTENTIALY PROBLAMETIC

    public GameObject(String name) {
        this.name = name;
        this.components = new ArrayList<>();
        this.transform = new Transform();
        zIndex = DEFAULT_Z_INDEX;
        this.uid = ID_COUNTER++;
    }

    public GameObject(String name, int zIndex) {
        this.name = name;
        this.components = new ArrayList<>();
        this.transform = new Transform();
        this.zIndex = zIndex;
        this.uid = ID_COUNTER++;
    }

    public GameObject(String name, Transform transform) {
        this.name = name;
        this.components = new ArrayList<>();
        this.transform = transform;
        zIndex = DEFAULT_Z_INDEX;
        this.uid = ID_COUNTER++;
    }

    public GameObject(String name, Transform transform, int zIndex) {
        this.name = name;
        this.components = new ArrayList<>();
        this.transform = transform;
        this.zIndex = zIndex;
        this.uid = ID_COUNTER++;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : "Error: Casting component.";
                }
            }
        }

        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i=0; i < components.size(); i++) {
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public void addComponent(Component c) {
        c.generateId();
        this.components.add(c);
        c.gameObject = this;
    }

    public void update(float dt) {
        for (int i=0; i < components.size(); i++) {
            components.get(i).update(dt);
        }
    }

    public void start() {
        isDead=false;
        for (int i=0; i < components.size(); i++) {
            components.get(i).start();
        }
    }

    public int getZIndex(){
        return zIndex;
    }

    //TODO
    public void setZIndex(int zIndex){
        if(this.zIndex==zIndex)
            return;
        this.zIndex = zIndex;
        for (int i=0; i < components.size(); i++) {
           if(components.get(i).getClass() == SpriteRenderer.class) {
               ((SpriteRenderer)components.get(i)).setDirtyZIndexFlag(true);
           }
        }
    }

    public void imgui() {

        if(ImGui.collapsingHeader("Object")) {

            ImGui.text("Id : " + getUID());
            this.name = MyImGui.inputText("Name: ", this.name);

            setZIndex(MyImGui.dragInt("zIndex : ", zIndex ));

            try {
                Field[] fields = this.transform.getClass().getDeclaredFields();
                for (Field field : fields) {
                    boolean isTransient = Modifier.isTransient(field.getModifiers());
                    if (isTransient) {
                        continue;
                    }

                    boolean isPrivate = Modifier.isPrivate(field.getModifiers());
                    if (isPrivate) {
                        field.setAccessible(true);
                    }

                    Class type = field.getType();
                    Object value = field.get(this.transform);
                    String name = field.getName();

                    if (type == int.class) {
                        int val = (int) value;
                        int[] imInt = {val};

                        field.set(this.transform, MyImGui.dragInt(name + ": ", imInt[0]));

                    }else if (type == float.class && name=="rotation") {
                        float val = (float) value;
                        float[] imFloat = {val};
                        transform.setRotation(MyImGui.dragFloat(name + ": ", imFloat[0]));

                    } else if (type == float.class) {
                        float val = (float) value;
                        float[] imFloat = {val};
                        field.set(this.transform, MyImGui.dragFloat(name + ": ", imFloat[0]));

                    } else if (type == boolean.class) {
                        boolean val = (boolean) value;
                        if (ImGui.checkbox(name + ": ", val)) {
                            field.set(this.transform, !val);
                        }
                    } else if (type == Vector2f.class) {
                        MyImGui.drawVec2Control(name+ ": ", (Vector2f) value);

                    } else if (type == Vector3f.class) {
                        MyImGui.drawVec3Control(name+ ": ", (Vector3f) value);
                    } else if (type == Vector4f.class) {
                        Vector4f val = (Vector4f) value;
                        float[] imVec = {val.x, val.y, val.z, val.w};
                        if (ImGui.dragFloat4(name + ": ", imVec)) {
                            val.set(imVec[0], imVec[1], imVec[2], imVec[3]);
                        }
                    }

                    if (isPrivate) {
                        field.setAccessible(false);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (Component c : components) {
            if(!c.getShouldImgui())
                continue;
            if(ImGui.collapsingHeader(c.getClass().getSimpleName()))
                c.imgui();
        }

    }

    public List<Component> getAllComponents() {
        return this.components;
    }

    public int getUID() {
        return this.uid;
    }

    public static void init(int maxId) {
        ID_COUNTER = maxId;
    }

    public void destroy() {
        Window.getImguiLayer().getPropertiesWindow().setActiveGameObject(null);
        isDead = true;
        for(int i=0; i<components.size(); i++) {
            components.get(i).destroy();
        }
    }

    public void setNoSerialize() {
         doSerialize = false;
    }

    public boolean isSerialized(){
        return  doSerialize;
    }

    public boolean isDead() {
        return isDead;
    }

    public void editorUpdate(float dt) {
        for (int i=0; i < components.size(); i++) {
            components.get(i).editorUpdate(dt);
        }
    }

    public GameObject copy() {
        // TODO: come up with cleaner solution
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
                .enableComplexMapKeySerialization()
                .create();
        String objAsJson = gson.toJson(this);
        GameObject obj = gson.fromJson(objAsJson, GameObject.class);

        obj.generateUID();
        for (Component c : obj.getAllComponents()) {
            c.generateId();
        }

        SpriteRenderer sprite = obj.getComponent(SpriteRenderer.class);
        if (sprite != null && sprite.getTexture() != null) {
            sprite.setTexture(AssetPool.getTexture(sprite.getTexture().getFilepath()));
        }

        return obj;
    }

    public void generateUID() {
        this.uid = ID_COUNTER++;
    }


}
