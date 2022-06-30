package Game;

import EntityComponent.GameObject;
import Observers.EventSystem;
import Observers.Events.Event;
import Observers.Events.EventType;
import Observers.Observer;
import Physics2D.Physics2D;
import Renderer.DebugDraw;
import Renderer.Framebuffer;
import Scenes.LevelEditorScene;
import Scenes.Scene;
import Util.Settings;
import Util.Time;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;


public class Window implements Observer {

	private int width, height;
    private String title;
    private long glfwWindow;
    private static ImGuiLayer imguiLayer;
    private static Window window = null;
    private long audioContext;
    private long audioDevice;
    private static Scene currentScene;
    float r=1f,g=1f,b=1f,a=1f;
    private Framebuffer framebuffer;
    private static boolean runTimePlaying = false; //if editor is not being editor update needs to be called

    private Window() {
        this.width = Settings.SCREEN_WIDTH;
        this.height = Settings.SCREEN_HEIGHT;
        this.title = Settings.TITLE;
        EventSystem.addObservers(this);
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }

        return Window.window;
    }

    public static Framebuffer getFramebuffer() {
        return get().framebuffer;
    }

    public static float getTargetAspectRatio() {
        return Settings.PROJECTION_WIDTH/Settings.PROJECTION_HEIGHT;
    }

    public static ImGuiLayer getImguiLayer() {
        return imguiLayer;
    }

    public static Physics2D getPhysics() {
        return currentScene.getPhysics();
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Destroy the audio context
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);

        // Free the memory

        //glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and the free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }

    public void init() {

        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        // Create the window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create the GLFW window.");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback );
        glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });

        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(glfwWindow);

        // Initialize the audio device
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            assert false : "Audio library not supported.";
        }

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        this.imguiLayer = new ImGuiLayer(glfwWindow);
        this.imguiLayer.initImGui();

        framebuffer = new Framebuffer(1920,1088);
        glViewport(0,0,1920, 1088);
        EventSystem.notify(null, new Event(EventType.LoadLevel));
        // Window.changeScene(new LevelEditorScene());
    }

    public static void changeScene(LevelEditorScene newScene) {
        if(currentScene!=null)
            currentScene.destroy();

        currentScene = newScene;
        currentScene.load();
        currentScene.init();
        currentScene.start();
    }

    public void loop() {

    	float beginTime = Time.getTime();
    	float endTime;
    	float dt=-1;

        while (!glfwWindowShouldClose(glfwWindow)) {
            // Poll events
            glfwPollEvents();
            DebugDraw.beginFrame();

            this.framebuffer.bind();
            glClearColor(  r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT);

            if(dt>=0) {
                if(isRunTimePlaying())
                    currentScene.update(dt);
                else
                    currentScene.update(dt);
                DebugDraw.draw();
            }
            this.framebuffer.unbind();
            this.imguiLayer.update(dt, currentScene);
            glfwSwapBuffers(glfwWindow);
            MouseListener.endFrame();
            endTime=Time.getTime();
            dt = endTime-beginTime;
            beginTime=Time.getTime();

        }

    }

    public static Scene getCurrentScene(){
        return get().currentScene;
    }

    public static int getHeight(){
        return get().height;
    }

    public  static int getWidth(){
        return get().width;
    }

    public static void setWidth(int newWidth) {
        get().width = newWidth;
    }

    public static void setHeight(int newHeight) {
        get().height = newHeight;
    }

    public static boolean isRunTimePlaying() {
        return runTimePlaying;
    }

    public void setRunTimePlaying(boolean runTimePlaying) {
        this.runTimePlaying = runTimePlaying;
    }

    @Override
    public void onNotify(GameObject object, Event event) {
        switch(event.type){
            case GameEngineStartPlay : System.out.println("Staring play");
                this.runTimePlaying=true;
                try {
                    currentScene.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                changeScene(new LevelEditorScene());
                break;

            case GameEngineStopPlay : System.out.println("Stopping");
                this.runTimePlaying=false;
                changeScene(new LevelEditorScene());
                break;

            case LoadLevel: changeScene(new LevelEditorScene());
                break;

            case SaveLevel:
                try {
                    currentScene.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


}