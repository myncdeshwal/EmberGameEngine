package Scenes;

import Components.DragAndDrop;
import EntityComponent.Component;
import EntityComponent.GameObject;
import EntityComponent.SpriteSheet;
import GameMechanics.ClimbingSowlyFallingBlocks;
import Gson.ComponentDeserializer;
import Gson.GameObjectDeserializer;
import Physics2D.Physics2D;
import Renderer.Renderer;
import Renderer.Camera;
import Util.AssetPool;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Scene {

	protected Renderer renderer = new Renderer();
	protected Camera camera;
	private boolean isRunning = false;
	public List<GameObject> gameObjects = new ArrayList<>();
	protected boolean updating = true;
	protected boolean adding = true;
	protected Physics2D physics2D;
	protected transient GameObject specialUseGO = new GameObject("LevelEditorUtils");

	public Scene() {

	}
	
	public void init() {
		
	}

	public void start(){
		for(int i=0; i<gameObjects.size(); i++ ) {
			GameObject go = gameObjects.get(i);
			go.start();
			this.renderer.add(go);
			this.physics2D.add(go);
		}
		isRunning=true;
	}

	public void update(float dt){
		this.camera.adjustProjection();

		this.physics2D.update(dt);

		updating = true;

		for (GameObject go : this.gameObjects) {
			if(!go.isDead()){
				go.update(dt);
			}
			else{
				this.renderer.destroyGameObject(go);
				this.physics2D.destroyGameObject(go);
				gameObjects.remove(go);
			}
		}

		updating = false;

		this.renderer.render();

		specialUseGO.update(dt);

	}

	public void addGameObjectToScene(GameObject go){
		adding = true;
		if(!isRunning){
			gameObjects.add(go)	;
		}
		else {
			gameObjects.add(go);
			go.start();
			this.renderer.add(go);
			this.physics2D.add(go);
		}
		adding = false;
	}

	protected void loadResources(){
		//sounds
		AssetPool.addSound("assets/sounds/main-theme-overworld.ogg", true);
		AssetPool.addSound("assets/sounds/flagpole.ogg", false);
		AssetPool.addSound("assets/sounds/break_block.ogg", false);
		AssetPool.addSound("assets/sounds/bump.ogg", false);
		AssetPool.addSound("assets/sounds/coin.ogg", false);
		AssetPool.addSound("assets/sounds/gameover.ogg", false);
		AssetPool.addSound("assets/sounds/jump-small.ogg", false);
		AssetPool.addSound("assets/sounds/mario_die.ogg", false);
		AssetPool.addSound("assets/sounds/pipe.ogg", false);
		AssetPool.addSound("assets/sounds/powerup.ogg", false);
		AssetPool.addSound("assets/sounds/powerup_appears.ogg", false);
		AssetPool.addSound("assets/sounds/stage_clear.ogg", false);
		AssetPool.addSound("assets/sounds/stomp.ogg", false);
		AssetPool.addSound("assets/sounds/kick.ogg", false);
		AssetPool.addSound("assets/sounds/invincible.ogg", false);

		//Everything else
		AssetPool.getShader("assets/shaders/default.glsl");
		AssetPool.addSpriteSheet("assets/spriteSheets/spriteSheet.png",
								new SpriteSheet(AssetPool.getTexture("assets/spriteSheets/spriteSheet.png"),
								16,16,26,0));
		AssetPool.addSpriteSheet("assets/spriteSheets/decorationsAndBlocks.png",
								new SpriteSheet(AssetPool.getTexture("assets/spriteSheets/decorationsAndBlocks.png"),
								16,16,81,0));
		AssetPool.addSpriteSheet("assets/spriteSheets/gizmos.png",
				new SpriteSheet(AssetPool.getTexture("assets/spriteSheets/gizmos.png"),
						24, 48, 3, 0));
		AssetPool.addSpriteSheet("assets/spriteSheets/bigSpriteSheet.png",
				new SpriteSheet(AssetPool.getTexture("assets/spriteSheets/bigSpriteSheet.png"),
						16,32,42,0));


	}

	public Camera getCamera(){
		return this.camera;
	}

	public void sceneImgui() {
		imgui();
	}

	public void imgui() {

	}

	public void save() throws Exception{

		if(DragAndDrop.safeToSave) {

			Gson gson = new GsonBuilder()
					.setPrettyPrinting()
					.registerTypeAdapter(Component.class, new ComponentDeserializer())
					.registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
					.enableComplexMapKeySerialization()
					.create();

			try {
				FileWriter writer = new FileWriter("level.txt");
				List<GameObject> gameObjectsToSerialize = new ArrayList<>();
				for(int i=0; i<this.gameObjects.size(); i++){
					if(this.gameObjects.get(i).isSerialized())
						gameObjectsToSerialize.add(this.gameObjects.get(i));
				}
				writer.write(gson.toJson(gameObjectsToSerialize));
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			System.out.println("not safe to save");

	}

	public void load() {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(Component.class, new ComponentDeserializer())
				.registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
				.enableComplexMapKeySerialization()
				.create();

		String inFile = "";
		try {
			inFile = new String(Files.readAllBytes(Paths.get("level.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!inFile.equals("")) {
			int maxCompId = -1;
			int maxGoId = -1;
			GameObject[] objs = gson.fromJson(inFile, GameObject[].class);
			for (int i=0; i < objs.length; i++) {
				//objs[i].addComponent(new ClimbingSowlyFallingBlocks());
				addGameObjectToScene(objs[i]);
				for (Component c : objs[i].getAllComponents()) {
					if (c.uid() > maxCompId) {
						maxCompId = c.uid();
					}
				}
				if (objs[i].getUID() > maxGoId) {
					maxGoId = objs[i].getUID();
				}
			}

			maxCompId++;
			maxGoId++;
			Component.init(maxCompId);
			GameObject.init(maxGoId);
		}
	}

	public GameObject getGameObject(int gameObjectId) {
		Optional<GameObject> result = this.gameObjects.stream()
				.filter(gameObject -> gameObject.getUID() == gameObjectId)
				.findFirst();
		return result.orElse(null);
	}


	public void destroy(){
		for(GameObject go: gameObjects){
			go.destroy();
			this.renderer.destroyGameObject(go);
			this.physics2D.destroyGameObject(go);
		}
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public void editorUpdate(float dt){

	}

	public GameObject getSpecialUseGO() {
		return specialUseGO;
	}

    public List<GameObject> getGameObjects() {
		return gameObjects;
    }

	public Physics2D getPhysics() {
		return physics2D;
	}
}
