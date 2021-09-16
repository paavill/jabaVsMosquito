import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.*;
import java.nio.*;
import java.util.Scanner;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Renderer {

    private long window;

    public void run() {
        System.out.println("Привет LWJGL " + Version.getVersion() + "!");

        init();
        try {
            loop();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Не удалось иницилизировать GLFW.");
        }

        glfwDefaultWindowHints();

        window = glfwCreateWindow(1024, 768, "Hello, World!", NULL, NULL);

        if (window == NULL) {
            throw new RuntimeException("Не удалось создать окно!");
        }
        //вынести лямбу в InputHandler
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        glfwSetFramebufferSizeCallback(window, InputHandler::resizeWindow);

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
    }

    private void loop() throws IOException {
        GL.createCapabilities();

        String shadersCompileExceptionsMessages = "";
        int logMessage[] = new int[1];

        CharSequence sourceVertexShader = graphicResourceLoader.loadShader("src/main/java/VERTEX_SHADER.glsl");
        CharSequence sourceFragmentShader = graphicResourceLoader.loadShader("src/main/java/FRAGMENT_SHADER.glsl");


        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, sourceVertexShader);
        glCompileShader(vertexShader);

        shadersCompileExceptionsMessages += "Vertex shader compiling: " + glGetShaderInfoLog(vertexShader) + "\n";

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, sourceFragmentShader);
        glCompileShader(fragmentShader);

        shadersCompileExceptionsMessages += "Fragment shader compiling: " + glGetShaderInfoLog(fragmentShader) + "\n";

        int shederProgram = glCreateProgram();
        glAttachShader(shederProgram, vertexShader);
        glAttachShader(shederProgram, fragmentShader);
        glLinkProgram(shederProgram);

        shadersCompileExceptionsMessages += "Shader program linking: " + glGetProgramInfoLog(shederProgram) + "\n";

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        System.out.println(shadersCompileExceptionsMessages);

        float vertices[] = {
                -0.5f, -0.5f, 0.0f, // левая вершина
                0.5f, -0.5f, 0.2f, // правая вершина
                0.0f,  0.5f, 0.5f  // верхняя вершина
        };

        int VBO[] = new int[1];
        int VAO[] = new int[1];
        glGenBuffers(VBO);
        glGenVertexArrays(VAO);

        glBindVertexArray(VAO[0]);
        glBindBuffer(GL_ARRAY_BUFFER, VBO[0]);
        glBufferData(GL_ARRAY_BUFFER,vertices,GL_STATIC_DRAW);

        glVertexAttribPointer(0,3,GL_FLOAT,false,0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);

        while (!glfwWindowShouldClose(window)) {
            glClearColor(
                    (float) Math.abs(Math.sin(glfwGetTime())),
                    (float) Math.abs(Math.cos(glfwGetTime())),
                    (float) Math.abs(Math.sin(2.0 * glfwGetTime())),
                    0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shederProgram);
            glBindVertexArray(VAO[0]); // поскольку у нас есть только один VAO, то нет необходимости связывать его каждый раз (но мы сделаем это, чтобы всё было немного организованнее)

            glDrawArrays(GL_TRIANGLES, 0, 3);
            glfwSwapBuffers(window);

            glUseProgram(0);
            glBindVertexArray(0);

            glfwPollEvents();
        }
    }
}