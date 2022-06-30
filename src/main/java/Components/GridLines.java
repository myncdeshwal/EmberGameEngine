package Components;

import EntityComponent.Component;
import Game.Window;
import Renderer.DebugDraw;
import Renderer.Line2D;
import Util.Settings;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GridLines extends Component {

    @Override
    public void start(){
        shouldImgui = false;
    }

    @Override
    public void editorUpdate(float dt){

        Vector2f cameraPos = Window.getCurrentScene().getCamera().position;

        float firstX = (int)(cameraPos.x/Settings.GRID_WIDTH) * Settings.GRID_WIDTH;
        float firstXF =cameraPos.x;
        float firstY =(int)(cameraPos.y/Settings.GRID_HEIGHT) * Settings.GRID_HEIGHT;
        float firstYF =cameraPos.y;

        int numVerticalLines =(int) Math.ceil(Settings.PROJECTION_WIDTH/Settings.GRID_WIDTH);
        int numHorizontalLines =(int) Math.ceil(Settings.PROJECTION_HEIGHT/Settings.GRID_HEIGHT);

        //int maxLines = Math.max(numHorizontalLines,numVerticalLines);

        // draws vertical lines
        for(int awooooo=0; awooooo<numVerticalLines; awooooo++){
            DebugDraw.addLine2D(
                    new Vector2f(firstX + awooooo * Settings.GRID_WIDTH,firstYF),
                    new Vector2f(firstX + awooooo * Settings.GRID_WIDTH,firstYF + Settings.PROJECTION_HEIGHT),
                    new Vector3f(0.2f,0.2f,0.2f),1);
        }


        // draws horizontal lines
            for (int i = 0; i < numHorizontalLines; i++) {

                DebugDraw.addLine2D(new Vector2f(firstXF, firstY + (i * Settings.GRID_HEIGHT)),
                        new Vector2f(firstXF + Settings.PROJECTION_WIDTH, firstY + (i * Settings.GRID_HEIGHT)),
                        new Vector3f(0.2f, 0.2f, 0.2f), 1);
            }

            //x axis
            DebugDraw.addLine2D(new Vector2f(Window.getCurrentScene().getCamera().position.x,0),
                    new Vector2f(Window.getCurrentScene().getCamera().position.x + Settings.SCREEN_WIDTH,0),
                    new Vector3f(1f,0,0), 1);

            //y axis
            DebugDraw.addLine2D(new Vector2f(0,Window.getCurrentScene().getCamera().position.y),
                new Vector2f(0,Window.getCurrentScene().getCamera().position.y + Settings.SCREEN_HEIGHT),
                new Vector3f(1f,0,0), 1);

    }

}
