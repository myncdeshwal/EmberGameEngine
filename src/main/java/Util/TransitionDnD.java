package Util;

import EntityComponent.GameObject;
import Components.Sprite;
import EntityComponent.SpriteRenderer;
import EntityComponent.SpriteSheet;
import Game.MouseListener;
import Game.Transform;
import Game.Window;
import GameComponents.AnimationState;
import GameComponents.StateMachine;
import org.joml.Vector2f;

public class TransitionDnD {

    public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY) {

        GameObject block = new GameObject("Sprite_Object_Gen",
                new Transform(new Vector2f(MouseListener.getOrthoX(), MouseListener.getOrthoY()), new Vector2f(sizeX, sizeY)), 0);
        SpriteRenderer renderer = new SpriteRenderer();
        renderer.setSprite(sprite);
        block.addComponent(renderer);
        Window.getCurrentScene().addGameObjectToScene(block);
        return block;
    }

    public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY, int zIndex) {

        GameObject block = new GameObject("Sprite_Object_Gen",
                new Transform(new Vector2f(MouseListener.getOrthoX(), MouseListener.getOrthoY()), new Vector2f(sizeX, sizeY)), zIndex);
        SpriteRenderer renderer = new SpriteRenderer();
        renderer.setSprite(sprite);
        block.addComponent(renderer);
        Window.getCurrentScene().addGameObjectToScene(block);
        return block;
    }



}
