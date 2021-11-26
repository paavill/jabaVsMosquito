package main;

import game_objects.blocks.BlockType;
import input.InputManager;
import input.KeyBindings;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import renderer.Renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;

import static org.lwjgl.glfw.GLFW.*;

public class Game {

    private Window window;
    private InputManager inputManager;
    private KeyBindings bindings;

    private Renderer renderer;
    private Camera camera;

    private int FPS = 75;
    private int msPearFrame = 1000/FPS;
    private double realFps = Double.MAX_VALUE;

    private World world;
    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void run() {
        init();
        try {
            loop();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        window.destroy();
        world.destroy();
        this.singleThreadPool.shutdown();
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
        Tuple<Float, Float> center = new Tuple<Float, Float>(window.getExtent().first / 2,  window.getExtent().second / 2);
        camera = new Camera(
                new Vector3f(0f, 0f, 0f),
                center,
                -90.0f, -40.0f, 0.05f, 0.3f);
        renderer = new Renderer(window, camera);
        inputManager = new InputManager(window);
        bindings = new KeyBindings(inputManager);

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
            inputManager.handleEvents();
            float toD = (float) delta;
            world.getPlayer().getMainCamera().setCameraMoveSpeedPercentOfDefault(toD);
            world.updateEntity();

            Runnable task = () -> {
                try {
                    //внутри метода надо менять метод для перехода по вериям генерации

                    world.update();

                } catch (ExecutionException |InterruptedException e) {
                    e.printStackTrace();
                }
            };
            this.singleThreadPool.submit(task);

            Runnable task2 = () -> {
                world.generateObjects();
            };
            for(int i = 0; i < Runtime.getRuntime().availableProcessors() - 5; i++){
                this.threadPool.execute(task2);
            }

            renderer.addObjectsToDraw(world);
            //не использовать при работе второй версии загрузки
            //renderer.deleteObjectsFromRender(world);
            renderer.deleteExtraObjectsToDraw(world);

            renderer.render();

            window.update(bindings);
            LinkedList<LinkedList<LinkedList<Tuple<Vector3f, BlockType>>>> types = world.getChunksManager().getToCollisionAreaByGlobalCoords(new Vector3f(this.world.getPlayer().getMainCamera().getCurrentPosition()), 8);
            System.out.println("new cube------------------------------------");
            for(int x = types.size() - 1; x > 0; x--){
                System.out.println("x:" + x);
                for (int y = types.get(0).size() - 1; y >= 0; y--) {
                    //System.out.println();
                    for (int z = types.get(0).get(0).size() - 1; z >= 0; z--) {
                        System.out.print(types.get(x).get(y).get(z).toString() + " ");
                    }
                    System.out.println();
                }
                System.out.println("\nend x:" + x);
            }
            System.out.println("new cube end------------------------------------");
            end = GLFW.glfwGetTime();

            delta = (end - start)*1000;
            if(delta < msPearFrame){
                Thread.sleep(msPearFrame - (long)(delta));
            }

            double sh = GLFW.glfwGetTime() - start;
            this.realFps = Math.min(1000/(sh*1000), this.realFps);
            glfwSetWindowTitle(window.getWindowDescriptor(),"JabaCraft fps:" + String.valueOf(1000/(sh*1000)) + " delta: " + String.valueOf(delta));
            //System.out.print(this.realFps);
            //System.out.print("   ");
            //System.out.println(1000/(sh*1000));
        }
    }
}