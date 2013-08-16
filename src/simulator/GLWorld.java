package simulator;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector4f;

public class GLWorld {
	
	HashMap<String, AbstractGLWorldLine> objectMap;
	Integer currentFrame;
	Integer maxFrames;
	boolean stopped;
	
	public GLWorld(HashMap<String, AbstractGLWorldLine> newMap) {
		objectMap = newMap;
		currentFrame = 0;
		for(String key : objectMap.keySet()) {
			maxFrames = objectMap.get(key).getMaxFrames()-1;
		}
		stopped = false;
	}
	
	public void toggleStop() {
		stopped = !stopped;
	}
	
	public void previous() {
		if(stopped && currentFrame > 0) currentFrame--;
	}
	
	public void next() {
		if(stopped && currentFrame < maxFrames-1) currentFrame++;
	}
	
	public void update() {
		if(currentFrame >= maxFrames) {
			stopped = true;
		}
		if(!stopped) currentFrame++;
		draw(currentFrame);
	}
	
	private void draw(Integer frame) {
		for(String key : objectMap.keySet()) {
			AbstractGLWorldLine object = objectMap.get(key);
			GL11.glColor3f(object.getColor().x, object.getColor().y, object.getColor().z);
			GL11.glBegin(GL11.GL_TRIANGLES);
				for(Vector4f vector : object.getFrame(frame)) {
					GL11.glVertex3f(vector.x, vector.y, vector.z);
				}
			GL11.glEnd();
		}
	}

}
