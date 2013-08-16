package simulator;

import java.io.Serializable;

import org.lwjgl.util.vector.Vector3f;

public class SpecialGLWorldLine extends AbstractGLWorldLine implements Serializable {
	
	private static final long serialVersionUID = -6093825912851281476L;
	
	Vector3f velocity;
	Double gamma;

	public SpecialGLWorldLine(int objectSize, Vector3f velocity) {
		super(objectSize, velocity, "");
		
	}

}
