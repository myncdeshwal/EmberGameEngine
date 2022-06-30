package PlayableCharacters.Controllers;


import EntityComponent.Component;
import EntityComponent.GameObject;
import Game.KeyListener;
import Game.Window;
import GameComponents.StateMachine;
import Physics2D.ContactListener;
import Physics2D.Enums.BodyType;
import Physics2D.RaycastInfo;
import Renderer.DebugDraw;
import Util.Settings;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import Physics2D.Components.Rigidbody2D;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class MarioController extends Component {


    private Vector2f extraAcc= new Vector2f();
    private transient int goingLeft = 0;
    private transient int goingRight = 0;

    public float walkSpeed = 6f;
    public float jumpBoost = 0.5f;
    public float jumpImpulse = 3.0f;
    public float slowDownForce = 1f;
    public Vector2f terminalVelocity = new Vector2f(3.1f, 3.1f);//3.1f);
    public Boolean facingRight=true;

    public transient boolean onGround = false;
    public transient float groundAngle = 0.0f;
    private transient float groundDebounce = 0.0f;
    private transient float groundDebounceTime = 0.1f;
    private transient Rigidbody2D rb;
    private transient StateMachine stateMachine;
    private transient float bigJumpBoostFactor = 1.05f;
    private transient float playerWidth = Settings.GRID_WIDTH;
    private transient int jumpTime = 0;
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f lastVelocity = new Vector2f();
    private transient boolean isDead = false;
    private transient int enemyBounce = 0;
    private float g=0;
    private float inAir=0;
    ContactListener contactListener;
    private boolean onGroundLast5frames = false;
    private boolean ableToWallJump = false;

    @Override
    public void start() {
        this.rb = gameObject.getComponent(Rigidbody2D.class);
        this.stateMachine = gameObject.getComponent(StateMachine.class);
        this.rb.setGravityScale(0f);
    }


    @Override
    public void update(float dt) {
        Window.getCurrentScene().getCamera().position.y=(gameObject.transform.position.y-Settings.PROJECTION_HEIGHT/4);
        Window.getCurrentScene().getCamera().position.x=(gameObject.transform.position.x-Settings.PROJECTION_WIDTH/2);

        ableToWallJump();
        if(ableToWallJump && KeyListener.isKeyPressed(GLFW_KEY_Z) && !onGround){
            if(facingRight){
                velocity.x*=-2f;
                if(velocity.x>-1){
                    velocity.x=-1;
                }
                this.velocity.y = 2*jumpImpulse * (float) Math.cos(Math.toRadians(groundAngle));
            }else {
                velocity.x*=-2f;
                if(velocity.x<1){
                    velocity.x=1;
                }
                this.velocity.y = 2*jumpImpulse * (float) Math.cos(Math.toRadians(groundAngle));
            }

        }
        else if (KeyListener.isKeyPressed(GLFW_KEY_RIGHT) || KeyListener.isKeyPressed(GLFW_KEY_D)) {
            this.gameObject.transform.scale.x = playerWidth;
            this.acceleration.x = walkSpeed * (float)Math.cos(Math.toRadians(groundAngle));
            if(onGround)
            this.acceleration.y -= walkSpeed * (float)Math.sin(Math.toRadians(groundAngle));

            if (this.velocity.x < 0) {
                //this.stateMachine.trigger("switchDirection");
                //facingRight=true;
                this.velocity.x += slowDownForce;
            } else {
                this.stateMachine.trigger("startRunning");
            }
        }
        else if (KeyListener.isKeyPressed(GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW_KEY_A)) {
            this.gameObject.transform.scale.x = -playerWidth;
            this.acceleration.x = -walkSpeed * (float)Math.cos(Math.toRadians(groundAngle));
            if(onGround)
                this.acceleration.y = -walkSpeed * (float)Math.sin(Math.toRadians(groundAngle));

            if (this.velocity.x > 0) {
                //this.stateMachine.trigger("switchDirection");
                //facingRight=false;

                this.velocity.x -= slowDownForce;
            } else {
                this.stateMachine.trigger("startRunning");
            }
        }
        else {
            this.acceleration.x = 0;
            if (this.velocity.x > 0 && onGround) {
                this.velocity.x = Math.max(0, this.velocity.x - slowDownForce);
            } else if (this.velocity.x < 0 && onGround) {
                this.velocity.x = Math.min(0, this.velocity.x + slowDownForce);
            }


            if (this.velocity.x == 0) {
                this.stateMachine.trigger("stopRunning");
            }
        }

        checkOnGround();

        if ((KeyListener.isKeyPressed(GLFW_KEY_Z) || KeyListener.isKeyPressed(GLFW_KEY_SPACE)) && (jumpTime > 0 || onGround || groundDebounce > 0)) {
            if ((onGround || groundDebounce > 0) && jumpTime == 0) {
                jumpTime = 28;
                this.velocity.y = jumpImpulse * (float) Math.cos(Math.toRadians(groundAngle));
            } else if (jumpTime > 0) {
                jumpTime--;
                this.velocity.y = ((jumpTime / 2.2f) * jumpBoost) * (float) Math.cos(Math.toRadians(groundAngle));
            } else {
                this.velocity.y = 0;
            }
            groundDebounce = 0;
        } else if (!onGround) {
            if (this.jumpTime > 0) {
                this.velocity.y *= 0.35f;
                this.jumpTime = 0;
            }
            groundDebounce -= dt;
            this.acceleration.y = Window.getPhysics().getGravity().y * 1f;
        } else {
            this.acceleration.y = 0;
            groundDebounce = groundDebounceTime;
        }

        if(true){
        this.velocity.x += this.acceleration.x * dt;
        this.velocity.y += this.acceleration.y * dt;
        this.velocity.x = Math.max(Math.min(this.velocity.x, this.terminalVelocity.x), -this.terminalVelocity.x);
        this.velocity.y = Math.max(Math.min(this.velocity.y, this.terminalVelocity.y), -this.terminalVelocity.y);
        if(velocity.x<0 && facingRight){
            facingRight=false;
            stateMachine.trigger("switchDirection");
        }
        else if(velocity.x>0 && !facingRight){
                facingRight=true;
                stateMachine.trigger("switchDirection");
            }

        this.rb.setVelocity(this.velocity);
        this.rb.setAngularVelocity(0);
        if (!onGroundLast5frames) {
            stateMachine.trigger("jump");
        } else {
            stateMachine.trigger("stopJumping");
        }}
    }

    Boolean[] array1 = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};

    public void checkOnGround() {
        for(int i=0; i<15 ;i++){
            onGroundLast5frames = array1[i] || onGroundLast5frames;
        }
        Vector2f raycastLeftBegin = new Vector2f(this.gameObject.transform.position);
        float innerPlayerWidth = this.playerWidth * 0.8f;
        raycastLeftBegin.sub(innerPlayerWidth / 2.0f, 0.0f);
        float yVal = -0.13f ;


        Vector2f raycastLeftEnd = new Vector2f(raycastLeftBegin).add(0.0f, yVal );

        RaycastInfo infoLeft = Window.getPhysics().raycast(gameObject, raycastLeftBegin, raycastLeftEnd);

        Vector2f raycastRightBegin = new Vector2f(raycastLeftBegin).add(innerPlayerWidth, 0.0f);
        Vector2f raycastRightEnd = new Vector2f(raycastLeftEnd).add(innerPlayerWidth, 0.0f);
        RaycastInfo infoRight = Window.getPhysics().raycast(gameObject, raycastRightBegin, raycastRightEnd);

        Vector2f raycastMiddleBegin = new Vector2f(raycastLeftBegin).add(innerPlayerWidth/2, 0.0f);
        Vector2f raycastMiddleEnd = new Vector2f(raycastLeftEnd).add(innerPlayerWidth/2, 0.0f);
        RaycastInfo infoMiddle = Window.getPhysics().raycast(gameObject, raycastMiddleBegin, raycastMiddleEnd);

        onGround = (infoLeft.hit && infoLeft.hitObject != null) ||
                   (infoRight.hit && infoRight.hitObject != null)   ||
                   (infoMiddle.hit && infoMiddle.hitObject != null);
        GameObject groundObj = null;
        if(infoMiddle.hit && infoMiddle.hitObject != null)
            groundObj = infoMiddle.hitObject;
        else if(infoRight.hit && infoRight.hitObject != null)
            groundObj = infoRight.hitObject;
        else if(infoLeft.hit && infoLeft.hitObject != null)
            groundObj = infoLeft.hitObject;
        if(groundObj!=null) {
            groundAngle = groundObj.transform.getRotation();
        }

        //DebugDraw.addLine2D(raycastLeftBegin, raycastLeftEnd, new Vector3f(1, 0, 0));
        //DebugDraw.addLine2D(raycastRightBegin, raycastRightEnd, new Vector3f(1, 0, 0));
        //DebugDraw.addLine2D(raycastMiddleBegin, raycastMiddleEnd, new Vector3f(1, 0, 0));

        for(int i=1; i<15; i++)
            array1[i-1]=array1[i];
        array1[14]=onGround;
    }

    public void ableToWallJump(){
        if(facingRight) {
            Vector2f raycastBegin = new Vector2f(gameObject.transform.position.x, gameObject.transform.position.y-playerWidth/3);
            Vector2f raycastEnd = new Vector2f(gameObject.transform.position.x+playerWidth/1.7f, gameObject.transform.position.y-playerWidth/3);
            RaycastInfo info = Window.getPhysics().raycast(gameObject, raycastBegin, raycastEnd);
            //DebugDraw.addLine2D(raycastBegin, raycastEnd, new Vector3f(1, 0, 0));

            ableToWallJump = info.hit && info.hitObject != null;
        }
        else {
            Vector2f raycastBegin = new Vector2f(gameObject.transform.position.x, gameObject.transform.position.y-playerWidth/3);
            Vector2f raycastEnd = new Vector2f(gameObject.transform.position.x-playerWidth/1.7f, gameObject.transform.position.y-playerWidth/3);
            RaycastInfo info = Window.getPhysics().raycast(gameObject, raycastBegin, raycastEnd);
            //DebugDraw.addLine2D(raycastBegin, raycastEnd, new Vector3f(1, 0, 0));

            ableToWallJump = info.hit && info.hitObject != null;
        }

        System.out.println(ableToWallJump);

    }

}