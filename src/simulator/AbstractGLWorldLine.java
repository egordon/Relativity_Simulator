package simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public abstract class AbstractGLWorldLine implements Serializable {
	
	private static final long serialVersionUID = 8585299658725550760L;

	LinkedBlockingQueue<ArrayList<Vector4f>> lineDataRaw;
	HashMap<Integer, ArrayList<Vector4f>> lineDataFinal;
	int size;
	protected WLType type;
	Vector3f color;
	String name;
	
	public enum WLType {
		kNewton,
		kSpecial,
		kGeneral
	}
	
	/**
	 * Each object needs their own World Line. Boundaries are divided into triangles that are manipulated and drawn separately.
	 * @param objectSize Number of points to have on the boundary of the object. Must be divisible by 3.
	 */
	public AbstractGLWorldLine(int objectSize, Vector3f objectColor, String newName) {
		color = objectColor;
		size = objectSize;
		if(objectSize%3 != 0) {
			System.err.println("WARNING! Object size is not divisible by 3. We are drawing triangles here. You WILL have an error drawing!");
		}
		lineDataRaw = new LinkedBlockingQueue<ArrayList<Vector4f>>();
		lineDataFinal = new HashMap<Integer, ArrayList<Vector4f>>();
		name = newName;
	}
	
	public int getMaxFrames() {
		return lineDataFinal.size();
	}
	
	public int getSize() {
		return size;
	}
	/**
	 * Add item to the raw World Line. Used to store non-interpolated values.
	 * @param arr Array of Vector4fs, one for each point on the object.
	 */
	public void rawEnqueue(ArrayList<Vector4f> arr) {
		lineDataRaw.add(arr);
	}
	
	/**
	 * Pull a point of the raw World Line for Interpolation.
	 * @return First Raw Point added to the raw World Line.
	 */
	public ArrayList<Vector4f> rawDequeue() {
		return lineDataRaw.poll();
	}
	/**
	 * Functions like rawDequeue, but doesn't remove the point from the World Line and only returns one Vector4f.
	 * @param value Which Four Vector in the object that you want to pull. 
	 * @return
	 */
	public Vector4f peekRawValue(int value) {
		if(value >= size) return null;
		else {
			return lineDataRaw.peek().get(value);
		}
	}
	
	/**
	 * Add a value to the interpolated World Line. This is what is used during playback.
	 * @param frame
	 * @param arr
	 */
	public void insertFrame(Integer frame, ArrayList<Vector4f> arr) {
		lineDataFinal.put(frame, arr);
	}
	
	public ArrayList<Vector4f> getFrame(Integer frame) {
		return lineDataFinal.get(frame);
	}
	
	public boolean isFrameReady(Integer frame) {
		return !(lineDataFinal.get(frame) == null);
	}
	
	public WLType getWLType() {
		return type;
	}
	
	public Vector3f getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}

}
