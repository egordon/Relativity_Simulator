package newton;

import java.util.ArrayList;

import newton.services.NewtonGenerator;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import simulator.AbstractGLWorldLine;

public class NewtonGLWorldLine extends AbstractGLWorldLine{

	private static final long serialVersionUID = 2398006203749894367L;
	
	// Generation Variables
	float mass;
	Vector3f velocity;
	Vector3f center;
	Vector3f netForce;
	float sideLength;

	public NewtonGLWorldLine(float newMass, Vector3f initVelocity, Vector3f initPos, float newSideLength, Vector3f newColor, String newName) {
		super(36, newColor, newName); // All objects assumed to be cubes. A Cube has 12 triangles.
		mass = newMass;
		type = WLType.kNewton;
		velocity = new Vector3f(0.0f, 0.0f, 0.0f);
		netForce = new Vector3f(0.0f, 0.0f, 0.0f);
		center = initPos;
		sideLength = newSideLength;
		setPosition(0);
		velocity = initVelocity;
	}
	
	/**
	 * Newtonian deals only with cubes for simplicity.
	 * Adds a cube of given Side Length centered at the current position determined by the initial conditions and the applied forces.
	 * @param frame Frame at which to add the current position.
	 */
	public void setPosition(Integer frame) {
		// Update Velocities
		Vector3f acceleration = (Vector3f) netForce.scale(1.0f/mass);
		Vector3f.add(acceleration, velocity, velocity);
		
		//System.out.println("Velocity:" + velocity);
		
		// Update Position
		Vector3f.add((Vector3f)velocity, center, center);
		
		float time = Float.valueOf(frame.toString());
		
		// Construct 8 points of a cube based on current Position and sideLength
		Vector4f frontUpLeft    = new Vector4f(center.x - sideLength/2.0f, center.y + sideLength/2.0f, center.z + sideLength/2.0f, time);
		Vector4f frontUpRight   = new Vector4f(center.x + sideLength/2.0f, center.y + sideLength/2.0f, center.z + sideLength/2.0f, time);
		Vector4f frontDownLeft  = new Vector4f(center.x - sideLength/2.0f, center.y - sideLength/2.0f, center.z + sideLength/2.0f, time);
		Vector4f frontDownRight = new Vector4f(center.x + sideLength/2.0f, center.y - sideLength/2.0f, center.z + sideLength/2.0f, time);
		Vector4f backUpLeft     = new Vector4f(center.x - sideLength/2.0f, center.y + sideLength/2.0f, center.z - sideLength/2.0f, time);
		Vector4f backUpRight    = new Vector4f(center.x + sideLength/2.0f, center.y + sideLength/2.0f, center.z - sideLength/2.0f, time);
		Vector4f backDownLeft   = new Vector4f(center.x - sideLength/2.0f, center.y - sideLength/2.0f, center.z - sideLength/2.0f, time);
		Vector4f backDownRight  = new Vector4f(center.x + sideLength/2.0f, center.y - sideLength/2.0f, center.z - sideLength/2.0f, time);
		
		ArrayList<Vector4f> nextPoints = new ArrayList<Vector4f>(36);
		
		// Manually Add all 36 points to form 12 triangles.
		
		// Front Upper Left
		nextPoints.add(frontUpLeft);
		nextPoints.add(frontDownLeft);
		nextPoints.add(frontUpRight);
		
		// Front Lower Right
		nextPoints.add(frontUpRight);
		nextPoints.add(frontDownRight);
		nextPoints.add(frontDownLeft);
		
		// Back Upper Left
		nextPoints.add(backUpLeft);
		nextPoints.add(backDownLeft);
		nextPoints.add(backUpRight);

		// Back Lower Right
		nextPoints.add(backUpRight);
		nextPoints.add(backDownRight);
		nextPoints.add(backDownLeft);
		
		// Top Front Left
		nextPoints.add(backUpLeft);
		nextPoints.add(frontUpRight);
		nextPoints.add(frontUpLeft);
		
		// Top Back Right
		nextPoints.add(backUpLeft);
		nextPoints.add(frontUpRight);
		nextPoints.add(backUpRight);
		
		// Bottom Front Left
		nextPoints.add(backDownLeft);
		nextPoints.add(frontDownRight);
		nextPoints.add(frontDownLeft);
		
		// Bottom Back Right
		nextPoints.add(backDownLeft);
		nextPoints.add(frontDownRight);
		nextPoints.add(backDownRight);
		
		// Left Up Front
		nextPoints.add(frontUpLeft);
		nextPoints.add(backUpLeft);
		nextPoints.add(frontDownLeft);
		
		// Left Down Back
		nextPoints.add(backDownLeft);
		nextPoints.add(backUpLeft);
		nextPoints.add(frontDownLeft);
		
		// Right Up Front
		nextPoints.add(frontUpRight);
		nextPoints.add(backUpRight);
		nextPoints.add(frontDownRight);
		
		// Right Down Back
		nextPoints.add(backDownRight);
		nextPoints.add(backUpRight);
		nextPoints.add(frontDownRight);
		
		//// Add Points to World Line
		this.insertFrame(frame, nextPoints);

		// Reset Net Force
		netForce.set(0.0f, 0.0f, 0.0f);
	}
	
	/**
	 * Self-explanatory, adds force to the net Force
	 * which is used in recalculate() and cleared each loop
	 * @param newForce
	 */
	public void ApplyForce(Vector3f newForce) {
		Vector3f.add(netForce, newForce, netForce);
	}
	
	public float getMass() {
		return mass;
	}
	
	public Vector3f getCenter() {
		return this.center;
	}
	
	public float getSideLength() {
		return sideLength;
	}
	
	public void ApplyGravity(NewtonGLWorldLine obj) {
		Vector3f distance = new Vector3f(0.0f, 0.0f, 0.0f);
		// Get Distance Vector between objects
		Vector3f.sub(obj.getCenter(), center, distance);
		
		Vector3f newForce = new Vector3f(0.0f, 0.0f, 0.0f);
		
		// Normalize the distance to get direction of the force.
		distance.normalise(newForce);
		
		double lengthDivider = distance.lengthSquared();
		double collisionDistance = (this.sideLength/2 + obj.getSize()/2);
		
		// Check for collision based on distance (like spheres)
		if((distance.length() <= collisionDistance*3 && velocity.length() > collisionDistance*2)
				|| (distance.length() <= collisionDistance*2 && velocity.length() > collisionDistance)
				|| distance.length() <= collisionDistance) {
			this.applyCollision(obj, distance);
			obj.applyCollision(this, new Vector3f(-distance.x, -distance.y, -distance.z));
		} else {
		
			// Actual gravitational equation for Newton, THE PHYSICS!
			newForce.scale((float) (NewtonGenerator.G_CONST * mass * obj.getMass() / 
					(lengthDivider)));
			
			// Apply equal and opposite force to other object
			// MORE PHYSICS!
			obj.ApplyForce(new Vector3f(-newForce.x, -newForce.y, -newForce.z));
			this.ApplyForce(newForce);
		}
	}
	
	/**
	 * No Forces here, just recalculating the velocity during a collision.
	 * @param obj What is colliding with this object
	 * @param distance Distance between the two objects (so we don't have to recalculate it)
	 */
	public void applyCollision(NewtonGLWorldLine obj, Vector3f distance) {
		Vector3f newVelocity = new Vector3f(0.0f, 0.0f, 0.0f);
		
		// Subtract the projection of velocity onto the distance vector twice
		// This is equivalent to reflecting across the plane that is
		// normal to the distance vector.
		Vector3f.sub(velocity, 
				(Vector3f)distance.scale(2 * (Vector3f.dot(velocity, distance)/Vector3f.dot(distance, distance))), 
				newVelocity);
		velocity = new Vector3f(newVelocity);
	}

}
