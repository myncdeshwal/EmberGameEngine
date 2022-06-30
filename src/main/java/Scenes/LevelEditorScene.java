package Scenes;

import Components.*;
import Editor.PropertiesWindow;
import EntityComponent.*;
import Game.KeyListener;
import Game.MouseListener;
import Game.Sound;
import Game.Window;
import GameComponents.MouseControls;
import Physics2D.Physics2D;
import PlayableCharacters.Mario;
import Renderer.Camera;
import Util.AssetPool;
import Util.Settings;
import Util.Time;
import Util.TransitionDnD;
import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Vector2f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;import java.io.File;
import java.util.Collection;

public class LevelEditorScene extends Scene {

	private transient GameObject specialUseGO = new GameObject("LevelEditorUtils");
	SpriteRenderer obj1SpriteRenderer;
	SpriteSheet spritesDnB;
	boolean flag =true;
	private int coolDown=0;
	private boolean delete=true;

	public LevelEditorScene(){


	}

	@Override
	public void init() {

		loadResources();
		this.physics2D = new Physics2D();
		this.camera = new Camera(new Vector2f(Camera.lastPosition.x,Camera.lastPosition.y));
		spritesDnB = AssetPool.getSpriteSheet("assets/spriteSheets/decorationsAndBlocks.png");
		SpriteSheet gizmos = AssetPool.getSpriteSheet("assets/spriteSheets/gizmos.png");
		specialUseGO.addComponent(new GizmoSystem(gizmos));
		specialUseGO.addComponent(new EditorCamera(camera));
		specialUseGO.addComponent(new GridLines());
		specialUseGO.addComponent(new MouseControls());
		specialUseGO.start();
	}

	@Override
	public void update(float dt) {

		this.camera.adjustProjection();

		if(!Window.isRunTimePlaying())
			specialUseGO.editorUpdate(dt);
		else
			this.physics2D.update(dt);

		if(coolDown>0)
			coolDown--;

		specialUseGO.update(dt);

		// main updation
		for (int i=0; i < gameObjects.size(); i++) {
			GameObject go = gameObjects.get(i);
			if(!Window.isRunTimePlaying())
				go.editorUpdate(dt);
			else
				go.update(dt);

			if (go.isDead()) {
				gameObjects.remove(i);
				this.renderer.destroyGameObject(go);
				this.physics2D.destroyGameObject(go);
				i--;
			}
		}

		this.renderer.render();

		if(!Window.isRunTimePlaying()) {
			scaling();
			cameraMovement(dt);
			deleteingObject();
		}
		if(coolDown<=0) {
			System.out.println("FPS: " + (1.0 / dt) + " " + Time.getTime());
		}
	}


	@Override
	public void imgui() {

		imguiModeSelector();
		ImGui.begin("Level Editor Tools");
		specialUseGO.imgui();
		ImGui.end();

		ImGui.begin("Test window");
		DragAndDrop dragAndDrop = new DragAndDrop();

		if (ImGui.beginTabBar("WindowTabBar")) {
			if (ImGui.beginTabItem("Blocks And Decorations")) {
				ImVec2 windowPos = new ImVec2();
				ImGui.getWindowPos(windowPos);
				ImVec2 windowSize = new ImVec2();
				ImGui.getWindowSize(windowSize);
				ImVec2 itemSpacing = new ImVec2();
				ImGui.getStyle().getItemSpacing(itemSpacing);

				float windowX2 = windowPos.x + windowSize.x;
				for (int i = 0; i < spritesDnB.size(); i++) {
					Sprite sprite = spritesDnB.getSprite(i);
					float spriteWidth = sprite.getWidth() * 2;
					float spriteHeight = sprite.getHeight() * 2;
					int id = sprite.getTexId();
					Vector2f[] texCoords = sprite.getTexCoords();

					ImGui.pushID(i);
					if (ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
						GameObject temp = TransitionDnD.generateSpriteObject(sprite, Settings.GRID_WIDTH, Settings.GRID_HEIGHT);
						dragAndDrop.pickup(temp, sprite);
					}
					ImGui.popID();

					ImVec2 lastButtonPos = new ImVec2();
					ImGui.getItemRectMax(lastButtonPos);
					float lastButtonX2 = lastButtonPos.x;
					float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
					if (i + 1 < spritesDnB.size() && nextButtonX2 < windowX2) {
						ImGui.sameLine();
					}
				}

				ImGui.endTabItem();
			}

			if (ImGui.beginTabItem("Characters")) {

				SpriteSheet playerSprites = AssetPool.getSpriteSheet("assets/spriteSheets/spriteSheet.png");
				Sprite sprite = playerSprites.getSprite(0);
				float spriteWidth = sprite.getWidth() * 4;
				float spriteHeight = sprite.getHeight() * 4;
				int id = sprite.getTexId();
				Vector2f[] texCoords = sprite.getTexCoords();
				if (ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
					GameObject object = Mario.generateMario();
					dragAndDrop.pickup(object);
				}
				ImGui.sameLine();

				ImGui.endTabItem();
			}

			if (ImGui.beginTabItem("Sounds")) {
				Collection<Sound> sounds = AssetPool.getAllSounds();
				for (Sound sound : sounds) {
					File tmp = new File(sound.getFilepath());
					if (ImGui.button(tmp.getName())) {
						if (!sound.isPlaying()) {
							sound.play();
						} else {
							sound.stop();
						}
					}

					if (ImGui.getContentRegionAvailX() > 100) {
						//ImGui.sameLine();
					}
				}

				ImGui.endTabItem();
			}



			if (ImGui.beginTabItem("Sounds")) {
				Collection<Sound> sounds = AssetPool.getAllSounds();
				for (Sound sound : sounds) {
					File tmp = new File(sound.getFilepath());
					if (ImGui.button(tmp.getName())) {
						if (!sound.isPlaying()) {
							sound.play();
						} else {
							sound.stop();
						}
					}

					if (ImGui.getContentRegionAvailX() > 100) {
						ImGui.sameLine();
					}
				}

				ImGui.endTabItem();
			}
			ImGui.endTabBar();
		}

		ImGui.end();
	}

	private void imguiModeSelector(){
		ImGui.begin("Selections");
		String[] name = {"+","- "};
		ImGui.text("scale : "+ DragAndDrop.spriteScale);
		ImGui.text("deleting : " + delete);
		ImGui.text("CameraPos : " + (int)camera.position.x + " " + (int)camera.position.y);

		for(int i = 0; i<2; i++){
			if(ImGui.button(name[i])){
				switch(i){
					case 0 : DragAndDrop.spriteScale *= 2f;
						break;
					case 1 : DragAndDrop.spriteScale *= 0.5f;
						break;
				}
			}
		}
		ImGui.end();
	}

	private void cameraMovement(float dt) {
		if(KeyListener.isKeyPressed(GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW_KEY_RIGHT)){
			int sign = 1;
			if(KeyListener.isKeyPressed(GLFW_KEY_LEFT))
				sign=-1;
			this.camera.position.x += sign * dt * 5 ;
		}
		if(KeyListener.isKeyPressed(GLFW_KEY_UP) || KeyListener.isKeyPressed(GLFW_KEY_DOWN)){
			int sign = 1;
			if(KeyListener.isKeyPressed(GLFW_KEY_DOWN))
				sign=-1;			this.camera.position.y += sign * dt * 5;
		}
	}

	private void deleteingObject() {
		if(KeyListener.keyBeginPress(GLFW_KEY_DELETE)){
			delete=!delete;
		}
		//deleting hehe
		if(delete && MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)){
			for(int i=0; i<this.gameObjects.size(); i++){
				GameObject go = gameObjects.get(i);
				if( Math.abs(MouseListener.getOrthoX()-go.transform.position.x) < go.transform.scale.x/2
						&&  Math.abs(MouseListener.getOrthoY()-go.transform.position.y) < go.transform.scale.y/2) {
					if(gameObjects.get(i).getComponent(NonPickable.class)==null)
						gameObjects.get(i).destroy();
				}
			}
		}
	}

	private void scaling() {
		if( KeyListener.keyBeginPress(GLFW_KEY_KP_SUBTRACT)) {
			DragAndDrop.spriteScale *= 0.5f;
			coolDown =Settings.FPS/4;
		}
		if( KeyListener.keyBeginPress(GLFW_KEY_KP_ADD)) {
			DragAndDrop.spriteScale *= 2;
			coolDown=Settings.FPS/4;
		}
	}


}
