package Components;

import EntityComponent.Component;
import EntityComponent.SpriteRenderer;

import java.awt.font.FontRenderContext;

public class FontRenderer extends Component {

    @Override
    public void start() {
        if (gameObject.getComponent(SpriteRenderer.class) != null) {
            System.out.println("Found Font Renderer!");
        }
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void editorUpdate(float dt) {

    }


}