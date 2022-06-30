package Util;

import static org.lwjgl.glfw.GLFW.*;


public class Time {
    public static double timeStarted = glfwGetTime();

    public static float getTime() { return (float)((glfwGetTime() - timeStarted)); }

}
