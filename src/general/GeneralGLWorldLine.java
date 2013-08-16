package general;

import general.util.Tensor;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import simulator.AbstractGLWorldLine;

public class GeneralGLWorldLine extends AbstractGLWorldLine {
	
	
	private static final long serialVersionUID = -8418157767038049758L;
	
	private static final int POINTS = 12;
	
	Vector4f velocity;
	Vector4f center;

	public GeneralGLWorldLine(Vector4f initVelocity, Vector4f initPos, Vector3f objectColor,
			String newName) {
		super(POINTS, objectColor, newName);
		velocity = initVelocity;
		center = initPos;
	}
	
	public Tensor getVelocity() {
		Tensor retTensor = new Tensor(Tensor.CONTRAVARIANT);
		retTensor.set(velocity.w, 0);
		retTensor.set(velocity.x, 1);
		retTensor.set(velocity.y, 2);
		retTensor.set(velocity.z, 3);
		
		return retTensor;
	}
	
	public Tensor getPosition() {
		Tensor retTensor = new Tensor(Tensor.CONTRAVARIANT);
		retTensor.set(center.w, 0);
		retTensor.set(center.x, 1);
		retTensor.set(center.y, 2);
		retTensor.set(center.z, 3);
		
		return retTensor;
	}
	
	public void update(Vector4f acceleration) {
		Vector4f.add(velocity, acceleration, velocity);
		Vector4f.add(center, velocity, center);
		
		ArrayList<Vector4f> nextPoints = new ArrayList<Vector4f>(POINTS);
		
		Vector4f xIncrease = new Vector4f(center);
		Vector4f yIncrease = new Vector4f(center);
		Vector4f zIncrease = new Vector4f(center);
		xIncrease.x++;
		yIncrease.y++;
		zIncrease.z++;
		
		// Front facing triangle
		nextPoints.add(center);
		nextPoints.add(xIncrease);
		nextPoints.add(yIncrease);
		
		// Bottom Facing Triangle
		nextPoints.add(center);
		nextPoints.add(xIncrease);
		nextPoints.add(zIncrease);
		
		// Left Facing Triangle
		nextPoints.add(center);
		nextPoints.add(yIncrease);
		nextPoints.add(zIncrease);
		
		// Upper Right Triangle
		nextPoints.add(xIncrease);
		nextPoints.add(yIncrease);
		nextPoints.add(zIncrease);
		
		this.rawEnqueue(nextPoints);
	}

}
