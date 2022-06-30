package Renderer;

import EntityComponent.SpriteRenderer;
import EntityComponent.GameObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Renderer {

    private final int MAX_BATCH_SIZE = 1000;
    private List<RenderBatch> batches;
    public boolean safeToAdd = true;
    List<GameObject> GObuffer = new ArrayList<>();

    public Renderer() {
        this.batches = new ArrayList<>();
    }

    public synchronized void add(GameObject go) {
        SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
        if (spr != null) {
            add(spr);
        }
    }

    private void add(SpriteRenderer sprite) {
        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.hasRoom() && batch.getZIndex() == sprite.gameObject.getZIndex()) {
                batch.addSprite(sprite);
                added = true;
                break;
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, sprite.gameObject.getZIndex());
            newBatch.start();
            batches.add(newBatch);
            newBatch.addSprite(sprite);
            Collections.sort(batches);
        }
    }

    public void render() {
        for (RenderBatch batch : batches) {
            batch.render();
        }
        for (GameObject go: GObuffer){
            add(go);
        }
        GObuffer.clear();
    }

    public void addSafely(GameObject gameObject) {
        GObuffer.add(gameObject);
    }

    public void destroyGameObject(GameObject go) {
        if(go.getComponent(SpriteRenderer.class)==null) return;

        for(RenderBatch batch : batches){
            if(batch.destroyIfExists(go))
                return;
        }
    }


}