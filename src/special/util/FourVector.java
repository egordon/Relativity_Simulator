package special.util;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class FourVector extends Vector4f {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4119045749675231693L;
	
	public FourVector() {
		super();
	}
	
	public FourVector(float x, float y, float z, float t) {
		super(x,y,z,t);
	}
	
	public FourVector(Vector3f init) {
		super(init.x, init.y, init.z, 0);
	}
	
	public FourVector(float x, float y, float z) {
		super(x,y,z,0);
	}
	
	public float getT() {
		return w;
	}
	
	public FourVector GetLorentzTransform(Vector3f velocity, float gamma) {
		return GetLorentzTransform(-velocity.x, -velocity.y, -velocity.z, gamma);
	}
	
	public FourVector GetLorentzTransform(float velX, float velY, float velZ, float gamma) {
		Vector3f beta = new Vector3f(velX/Util.CSPEED, velY/Util.CSPEED, velZ/Util.CSPEED);
		if(beta.length() == 0) {
			return this.clone();
		}
		FourVector newVector = new FourVector(0.0f, 0.0f, 0.0f);
		newVector.w = gamma * (this.w*Util.CSPEED - this.x*beta.x - this.y*beta.y - this.z*beta.z) / Util.CSPEED;
		newVector.x = (-gamma * beta.x * Util.CSPEED * this.w) 
				+ this.x*(1+(gamma-1)*(beta.x*beta.x)/(beta.lengthSquared()))
				+ this.y*((gamma-1)*(beta.x*beta.y)/(beta.lengthSquared()))
				+ this.z*((gamma-1)*(beta.x*beta.z)/(beta.lengthSquared()));
		newVector.y = (-gamma * beta.y * Util.CSPEED * this.w) 
				+ this.y*(1+(gamma-1)*(beta.y*beta.y)/(beta.lengthSquared()))
				+ this.x*((gamma-1)*(beta.y*beta.x)/(beta.lengthSquared()))
				+ this.z*((gamma-1)*(beta.y*beta.z)/(beta.lengthSquared()));
		newVector.z = (-gamma * beta.z * Util.CSPEED * this.w) 
				+ this.z*(1+(gamma-1)*(beta.z*beta.z)/(beta.lengthSquared()))
				+ this.x*((gamma-1)*(beta.z*beta.x)/(beta.lengthSquared()))
				+ this.y*((gamma-1)*(beta.z*beta.y)/(beta.lengthSquared()));
		return newVector;
	}
	
	public FourVector clone() {
		return new FourVector(this.x, this.y, this.z, this.w);
	}

}
