package main;

import input.InputManager;
import input.KeyBindings;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import renderer.Renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;

public class Game {

    private Window window;
    private InputManager inputManager;
    private KeyBindings bindings;

    private Renderer renderer;
    private Camera camera;

    private int FPS = 60;
    private int msPearFrame = 1000/FPS;
    private double realFps = Double.MAX_VALUE;

    private World world;
    private ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public void run() {
        init();
        try {
            loop();
        } catch (Exception exception) {

        }
        window.destroy();
        world.destroy();
        this.threadPool.shutdown();
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Failed to init GLFW.");
        }

        window = new Window("JabaCraft", 1024, 768);
        inputManager = new InputManager(window);
        bindings = new KeyBindings(inputManager);

        Tuple<Float, Float> center = new Tuple<Float, Float>(window.getExtent().first / 2,  window.getExtent().second / 2);

        camera = new Camera(
                new Vector3f(0f, 0f, 0f),
                center,
                -90.0f, -40.0f, 1.7f, 0.3f);

        renderer = new Renderer(window, camera);
        Chunk.setBlocksModels(new HashMap<>(BlocksModelsInitializer.init()));
        world = new World(camera, bindings);
    }

    private void loop() throws IOException, InterruptedException {
        double start;
        double end;
        double delta = 0;
        double er = 0;
        //TODO: Добавить DeltaTime
        while (!window.shouldClose()) {
            start = GLFW.glfwGetTime();

            renderer.render();

            inputManager.handleEvents();
            float toD = (float) delta;
            //world.getPlayer().getMainCamera().setCameraMoveSpeedPercentOfDefault(toD);
            world.updateEntity();

            Runnable task = () -> {
                try {
                    world.update();
                } catch (ExecutionException |InterruptedException e) {
                    e.printStackTrace();
                }
            };
            this.threadPool.submit(task);

            renderer.addObjectsToDraw(world);
            renderer.deleteObjectsFromRender(world);
            renderer.deleteExtraObjectsToDraw(world);

            window.update(bindings);
            end = GLFW.glfwGetTime();

            delta = end - start;
            if(delta*1000 < msPearFrame){
                Thread.sleep(msPearFrame - (long)(delta*1000));
            } else {
                System.out.println(end - start);
            }
            double sh = GLFW.glfwGetTime() - start;
            this.realFps = Math.min(1000/(sh*1000), this.realFps);
            System.out.print(this.realFps);
            System.out.print("   ");
            System.out.println(1000/(sh*1000));
        }
    }
}