package graphics;

import static graphics.InputOutput.clearIO;
import static graphics.InputOutput.initIO;
import static graphics.InputOutput.keyDown;
import static graphics.InputOutput.keyPress;
import static graphics.InputOutput.mousedX;
import static graphics.InputOutput.mousedY;
import static graphics.InputOutput.pollIO;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glViewport;
import graphics.glmodel.Camera;

import java.util.HashMap;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.glu.GLU;

import simulator.AbstractGLWorldLine;
import simulator.GLWorld;


public class DisplayInterface extends Thread {
	
	boolean isFinished;
	
	// Timing Variables
	int maxFPS;
	long fps,lastFPS, lastFrame;
	
	// Key Variables
	int fullscreenKey;
	int toggleMouse;
	boolean isFullScreen;
	
	// Camera Vars
	float xspeed, yspeed, zspeed, yaw, pitch;
	float xpos, ypos, zpos;
	float angle, pangle;
	final float MOVE_SPEED = 10;
	
	// GL Variables
	int moveQuadX, moveQuadY;
	Runtime myRuntime;
	GLWorld world;
	
	public DisplayInterface(HashMap<String, AbstractGLWorldLine> playbackMap) {
		
		// Initialize World
		world = new GLWorld(playbackMap);
		
		// Initialize Timing Vars
		isFinished = false;
		maxFPS = 60;
		
		//Initialize Keyboard Vars
		fullscreenKey = Keyboard.KEY_0;
		toggleMouse = Keyboard.KEY_E;
		isFullScreen = false;
		
		// Initialize InputOutput
		initIO();
		
		// Initialize Runtime
		myRuntime = Runtime.getRuntime();
	}
	
	public void run() {
		
		// Initialize Display and I/O
		try {
			Display.setDisplayMode(Display.getDesktopDisplayMode());
			Display.setTitle("Time: 1 Second Per Frame; FPS: ...");
			Display.setVSyncEnabled(true);
			Display.create();
			Mouse.setGrabbed(true);
		} catch(LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
			
		
		// Clear Everything!
		clearIO();
		lastFPS = getTime();
		
		//Initialize Variables
		moveQuadX = moveQuadY = 100;
		
		// Initialize OpenGL Here!
		glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(45.0f,(float)Display.getDisplayMode().getWidth()/(float)Display.getDisplayMode().getHeight(),0.1f,10000.0f);
		
        /*
        // Setup Lighting Buffer
        ByteBuffer temp = ByteBuffer.allocateDirect(16);
        temp.order(ByteOrder.nativeOrder());
        
        // Setup Lighting Information
        float[] light_ambient = { 0.0f, 0.0f, 0.0f, 50.0f };
        float[] light_diffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
        float[] light_specular = { 0.0f, 0.0f, 0.0f, 0.0f };
        //float[] light_position = { 0.0f, 0.0f, -1000.0f, 0.0f };
        
        // Map Lighting
        glLight(GL_LIGHT0, GL_AMBIENT, (FloatBuffer)temp.asFloatBuffer().put(light_ambient).flip());
        glLight(GL_LIGHT0, GL_DIFFUSE, (FloatBuffer)temp.asFloatBuffer().put(light_diffuse).flip());
        glLight(GL_LIGHT0, GL_SPECULAR, (FloatBuffer)temp.asFloatBuffer().put(light_specular).flip());
        glEnable(GL_LIGHT0);
        */
        
        // Setup Camera
        Camera.setCamera(      0f, 0f, 0f,         // position at origin
                0f, 0f, -1f,        // looking down Z axis
                0f, 1f, 0f );
        xspeed=yspeed=zspeed=yaw=pitch=angle=pangle = 0.0f;
        xpos = ypos = zpos = 0;
        
		// Enable Addons
		glEnable(GL_DEPTH_TEST);
		
		// Main Game Loop
		while(!isFinished) {
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			
			// Use Input Here!
			xspeed = yspeed = zspeed = 0;
			if(keyDown[Keyboard.KEY_D]) {
				zspeed -= MOVE_SPEED * -(float)Math.sin(Math.toRadians(angle));
                xspeed -= MOVE_SPEED * (float)Math.cos(Math.toRadians(angle));
			}
			if(keyDown[Keyboard.KEY_A]) {
				zspeed += MOVE_SPEED * -(float)Math.sin(Math.toRadians(angle));
                xspeed += MOVE_SPEED * (float)Math.cos(Math.toRadians(angle));
			}
			if(keyDown[Keyboard.KEY_W]) {
				zspeed += MOVE_SPEED * (float)Math.cos(Math.toRadians(angle));
                xspeed += MOVE_SPEED * (float)Math.sin(Math.toRadians(angle));
			}
			if(keyDown[Keyboard.KEY_S]) {
				zspeed -= MOVE_SPEED * (float)Math.cos(Math.toRadians(angle));
                xspeed -= MOVE_SPEED * (float)Math.sin(Math.toRadians(angle));
			}
			if(keyDown[Keyboard.KEY_UP]) yspeed-=10;
			if(keyDown[Keyboard.KEY_DOWN]) yspeed+=10;
			
			
			
			xpos += xspeed;
			ypos += yspeed;
			zpos += zspeed;
			//float[] light_position = {xpos, ypos, zpos, 0.0f};
			
			// Playback Controls
			if(keyPress[Keyboard.KEY_SPACE]) world.toggleStop();
			if(keyDown[Keyboard.KEY_LEFT]) world.previous();
			if(keyDown[Keyboard.KEY_RIGHT]) world.next();
		
						
			if(keyPress[fullscreenKey]) {
				if (Display.getDisplayMode().isFullscreenCapable()) {
					try {
						Display.setFullscreen(!isFullScreen);
					} catch(LWJGLException e) {
						System.err.println("Could not go to full screen!");
						e.printStackTrace();
					}
					isFullScreen = !isFullScreen;
				} else {
					System.out.println("DisplayMode is not Full Screen Capable");
				}
			}
			if(keyPress[toggleMouse]) Mouse.setGrabbed(!Mouse.isGrabbed());
			
			
			
			// Clear Buffers Here!
			glClear(GL_COLOR_BUFFER_BIT | 
					GL_DEPTH_BUFFER_BIT);
			
			// Render OpenGL Here!s
			//glColorMaterial ( GL11.GL_FRONT , GL_SPECULAR) ;
			glColor3f(0.5f, .5f, 1.0f);
			
			
			// Stationary Objects
			
			// Update Camera
            Camera.Move(-xspeed, -yspeed, -zspeed);
            Camera.RotateX(-pangle);
            pangle += pitch;
            Camera.RotateY(yaw);
            Camera.Render();
            Camera.RotateX(pangle);
            glLoadIdentity();
            Camera.Render();
            
            // Moving Objects
            world.update();
			
			// Detect if User detects Window Closed
			if(Display.isCloseRequested()) isFinished = true;
			if(keyPress[Keyboard.KEY_ESCAPE]) isFinished = true;
			
			// Update and Sync Display
			Display.sync(maxFPS);
			update();
			Display.update();
		}
		
		// Cleanup
		Mouse.setGrabbed(false);
		Display.destroy();
	}
	
	
	public void update() {
		// Update Everything!
		pollIO();
		updateFPS();
		
		// Update yaw and pitch of camera
		yaw = -mousedX*0.1f;
        pitch = mousedY*0.1f;
        angle += yaw;
        if(pangle-pitch<-90 && pitch<0) pitch = 0.0f;
        else if(pangle+pitch>90 && pitch>0) pitch = 0.0f;
        System.out.println("Used Memory: " + myRuntime.freeMemory()/1024);
	}
	
	
	
	/** 
	 * Calculate how many milliseconds have passed 
	 * since last frame.
	 * 
	 * @return milliseconds passed since last frame 
	 */
	public int getDelta() {
	    long time = getTime();
	    int delta = (int) (time - lastFrame);
	    lastFrame = time;
	 
	    return delta;
	}
	
	/**
	 * Get the accurate system time
	 * 
	 * @return The system time in milliseconds
	 */
	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			Display.setTitle("Time: 1 Second Per Frame; FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

}
