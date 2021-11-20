package main;

import game_objects.Player;
import input.KeyBindings;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.concurrent.*;

public class World {

    private Player player;
    private ChunksManager chunksManager = new ChunksManager(40);


    public World(Camera main, KeyBindings bindings) {

        try {
            this.chunksManager.generateChunks();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.player = new Player(main, bindings);
        this.player.move(new Vector3f(0, 100,0));
    }

    public ArrayList<ArrayList<Chunk>> getToDelete(){
        return chunksManager.getToDeleteChunks();
    }

    public void updateEntity(){
        player.update();
        chunksManager.setPlayerPosition(player.getPosition());
    }

    public void update() throws ExecutionException, InterruptedException {
        chunksManager.updateChunks();
    }

    public ChunksManager getChunksManager() {
        return chunksManager;
    }
}