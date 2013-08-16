package special.util;

import org.lwjgl.util.vector.Vector3f;

public class Util {

	public static float CAMERA_GAMMA = 1.0f;
	public static float CSPEED = 10.0f;
	public static Vector3f CAMERA_VELOCITY = new Vector3f(0.0f, 0.0f, 0.0f);

	public static float getGamma(Vector3f velocity) {
		if(velocity.length() < CSPEED) {
			return (float) (1 / Math.sqrt(1-(velocity.lengthSquared() / (CSPEED * CSPEED))));
		} else {
			System.err.println("ERROR! Requested velocity is not a valid reference frame!");
			System.exit(-1);
			return -1;
		}
	}

	public static void setCameraVelocity(Vector3f newVelocity) {
		CAMERA_VELOCITY = newVelocity;
		CAMERA_VELOCITY.x *= -1;
		CAMERA_VELOCITY.y *= -1;
		CAMERA_VELOCITY.z *= -1;
		CAMERA_GAMMA = getGamma(newVelocity);
	}

	public static void SetC(float newC) {
		CSPEED = newC;
	}

	public static void SetC(Double newC) {
		CSPEED = Float.parseFloat(newC.toString());
	}

	public static FourVector interpolate(FourVector leftPoint, FourVector rightPoint, Float newTime) {
		FourVector returnVector = new FourVector();
		returnVector.w = newTime;

		if(rightPoint.w == leftPoint.w || newTime == leftPoint.w) return leftPoint;

		returnVector.x = (rightPoint.x - leftPoint.x) / (rightPoint.w - leftPoint.w) * (newTime - leftPoint.w) + leftPoint.x;
		returnVector.y = (rightPoint.y - leftPoint.y) / (rightPoint.w - leftPoint.w) * (newTime - leftPoint.w) + leftPoint.y;
		returnVector.z = (rightPoint.z - leftPoint.z) / (rightPoint.w - leftPoint.w) * (newTime - leftPoint.w) + leftPoint.z;

		return returnVector;
	}

}
