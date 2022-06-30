package GameMechanics;

import EntityComponent.Component;
import Physics2D.Components.Rigidbody2D;

public class ClimbingSowlyFallingBlocks extends Component {
    Rigidbody2D rb;

    @Override
    public void start(){
        rb=gameObject.getComponent(Rigidbody2D.class);
    }

    @Override
    public void update(float dt){
        if(gameObject.getComponent(Rigidbody2D.class)!=null && gameObject.name!="mario")

                rb.setGravityScale(0.001f);


    }

}
