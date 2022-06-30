package Game;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {
    private static KeyListener instance;
    private int keyPressed[] = new int[350];
    private boolean keyBeginPress[] = new boolean[350];
    private static int lastAction=0;
    private static boolean lastKeyPressed[] = new boolean[350];
    private KeyListener() {

    }

    public static KeyListener get() {
        if (KeyListener.instance == null) {
            KeyListener.instance = new KeyListener();
        }

        return KeyListener.instance;
    }

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {

        if(key<350 && key>-1) {
    		if (action == GLFW_PRESS) {
                if(lastKeyPressed[key]==true)
                    get().keyBeginPress[key]=false;
                else
                    get().keyBeginPress[key]=true;
                get().keyPressed[key] = 1;
	        } else if (action == GLFW_RELEASE) {
                get().keyBeginPress[key]=false;
	            get().keyPressed[key] = 0;
            }
	  }
    }

    public static boolean isKeyPressed(int keyCode) {
         if(get().keyPressed[keyCode]==1)
             return  true;
         else
             return  false;
    }

    public static boolean keyBeginPress(int keyCode) {
        boolean result = get().keyBeginPress[keyCode];
        get().keyBeginPress[keyCode]=false;
        return result;
    }

}