package EntityComponent;

import Editor.MyImGui;
import PlayableCharacters.Controllers.MarioController;
import imgui.ImGui;
import imgui.type.ImInt;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class Component {

    public transient GameObject gameObject = null;
    private static int ID_COUNTER = 0;
    private int uid = -1;
    protected transient Boolean shouldImgui = true;

    public void start() {

    }

    public void update(float dt){

    }

    public void beginCollision(GameObject collidingObject, Contact contact,Vector2f hitNormal){
        System.out.println(gameObject.name);
    }
    public void endCollision(GameObject collidingObject, Contact contact,Vector2f hitNormal){

    }
    public void preSolve(GameObject collidingObject, Contact contact,Vector2f hitNormal){

    }
    public void postSolve(GameObject coliidingObject, Contact contact,Vector2f hitNormal){

    }
    public void imgui()  {
        try {
            Field[] fields = this.getClass().getDeclaredFields();
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
                Object value = field.get(this);
                String name = field.getName();
                if (type == int.class) {
                    int val = (int) value;
                    int[] imInt = {val};
                    field.set(this, MyImGui.dragInt(name + ": ", imInt[0]));
                } else if (type == float.class) {
                    float val = (float) value;
                    float[] imFloat = {val};
                    field.set(this, MyImGui.dragFloat(name + ": ", imFloat[0]));
                } else if (type == boolean.class) {
                    boolean val = (boolean) value;
                    if (ImGui.checkbox(name + ": ", val)) {
                        field.set(this, !val);
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
                } else if (type.isEnum()) {
                    String[] enumValues = getEnumValues(type);
                    String enumType = ((Enum)value).name();
                    ImInt index = new ImInt(indexOf(enumType, enumValues));
                    if (ImGui.combo(field.getName(), index, enumValues, enumValues.length)) {
                        field.set(this, type.getEnumConstants()[index.get()]);
                    }

                    if (isPrivate) {
                        field.setAccessible(false);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateId() {
        if (this.uid == -1) {
            this.uid = ID_COUNTER++;
        }
    }

    public int uid() {
        return this.uid;
    }

    public static void init(int maxId) {
        ID_COUNTER = maxId;
    }

    public void destroy() {

    }

    public Boolean getShouldImgui() {
        return shouldImgui;
    }

    public void setShouldImgui(Boolean shouldImgui) {
        this.shouldImgui = shouldImgui;
    }

    public void editorUpdate(float dt){}

    private <T extends Enum<T>> String[] getEnumValues(Class<T> enumType) {
        String[] enumValues = new String[enumType.getEnumConstants().length];
        int i = 0;
        for (T enumIntegerValue : enumType.getEnumConstants()) {
            enumValues[i] = enumIntegerValue.name();
            i++;
        }
            return enumValues;
        }

    private int indexOf(String str, String[] arr) {
        for (int i=0; i < arr.length; i++) {
            if (str.equals(arr[i])) {
                return i;
            }
        }
        return -1;
        }

}