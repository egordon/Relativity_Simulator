package graphics.glmodel;
import org.lwjgl.util.glu.*;

public class Camera {
	static final float PIdiv180 = 0.0174532925f;
	public static GL_Vector ViewDir;
	public static GL_Vector RightVector;
	public static GL_Vector UpVector;
	public static GL_Vector Position;
	public static float RotatedX, RotatedY, RotatedZ;
	
	public Camera() {
		setCamera( 	0f, 0f, 0f,         // position at origin
				0f, 0f, -1f,        // looking down Z axis
				0f, 1f, 0f );       // camera up axis is straight up Y axis
	}
	
	public Camera(float posx, float posy, float posz,
			float dirx, float diry, float dirz,
			float upx, float upy, float upz)
	{
		setCamera( posx,posy,posz, dirx,diry,dirz, upx,upy,upz );
	}
	
	/**
	 * Set the camera position, view direction and up vector.  NOTE: direction
	 * is direction the camera is facing NOT a target position (as in gluLookAt()).  
	 * 
	 * @param posx   Position of camera
	 * @param posy
	 * @param posz
	 * @param dirx   Direction camera is facing
	 * @param diry
	 * @param dirz
	 * @param upx    Up vector
	 * @param upy
	 * @param upz
	 */
	public static void setCamera(float posx, float posy, float posz,
			float dirx, float diry, float dirz,
			float upx, float upy, float upz)
	{
		if (upx == 0 && upy == 0 && upz == 0) {
			System.out.println("GLCamera.setCamera(): Up vector needs to be defined");
			upx=0; upy=1; upz=0;
		}
		if (dirx == 0 && diry == 0 && dirz == 0) {
			System.out.println("GLCamera.setCamera(): ViewDirection vector needs to be defined");
			dirx=0; diry=0; dirz=-1;
		}
		Position 	= new GL_Vector(posx, posy, posz);
		ViewDir 	= new GL_Vector(dirx, diry, dirz);
		UpVector 	= new GL_Vector(upx, upy, upz);
		RightVector	= GL_Vector.crossProduct(ViewDir, UpVector);
		RotatedX = RotatedY = RotatedZ = 0.0f;  // TO DO: should set these to correct values
	}
	
	/**
	 * Set the camera to look at a target.  Positions the camera on the same X and Y
	 * as the target, at the Z value specified by the distance param, looking down 
	 * the Z axis.
	 *  
	 * @param targetX     camera will face this XYZ
	 * @param targetY
	 * @param targetZ
	 * @param distance    distance from target
	 */
	public static void setCamera(float targetX, float targetY, float targetZ,  float distance)
	{
		Position 	= new GL_Vector(targetX, targetY, targetZ+distance);
		ViewDir 	= new GL_Vector(0, 0, -1);
		UpVector 	= new GL_Vector(0, 1, 0);
		RightVector	= GL_Vector.crossProduct(ViewDir, UpVector);
		RotatedX = RotatedY = RotatedZ = 0.0f;
	}
	
	/**
	 * Move camera position in the given direction
	 */
	public static void viewDir(GL_Vector direction) {
		ViewDir = direction;
		RightVector	= GL_Vector.crossProduct(ViewDir, UpVector);
	}
	
	/**
	 * Move camera position in the given direction
	 */
	public static void Move(GL_Vector Direction) {
		Position = GL_Vector.add(Position, Direction);
	}
	
	/**
	 * Move camera position in the given direction
	 */
	public static void Move(float x, float y, float z) {
		GL_Vector Direction = new GL_Vector(x,y,z);
		Position = GL_Vector.add(Position, Direction);
	}
	
	/**
	 * Move camera to the given xyz
	 */
	public static void MoveTo(float x, float y, float z) {
		Position = new GL_Vector(x, y, z);
	}
	
	public static void RotateX(float Angle) {
		RotatedX += Angle;
		
		//Rotate viewdir around the right vector:
		ViewDir = GL_Vector.normalize(
				GL_Vector.add(
						GL_Vector.multiply(ViewDir, (float) Math.cos(Angle * PIdiv180)),
						GL_Vector.multiply(UpVector, (float) Math.sin(Angle * PIdiv180))
				)
		);
		
		//now compute the new UpVector (by cross product)
		UpVector = GL_Vector.multiply(GL_Vector.crossProduct(ViewDir, RightVector), -1);
	}
	
	public static void RotateY(float Angle) {
		RotatedY += Angle;
		
		//Rotate viewdir around the up vector:
		ViewDir = GL_Vector.normalize(
				GL_Vector.sub(
						GL_Vector.multiply(ViewDir, (float) Math.cos(Angle * PIdiv180)),
						GL_Vector.multiply(RightVector, (float) Math.sin(Angle * PIdiv180))
				));
		
		//now compute the new RightVector (by cross product)
		RightVector = GL_Vector.crossProduct(ViewDir, UpVector);
	}
	
	public static void RotateZ(float Angle) {
		RotatedZ += Angle;
		
		//Rotate viewdir around the right vector:
		RightVector = GL_Vector.normalize(
				GL_Vector.add(
						GL_Vector.multiply(RightVector, (float) Math.cos(Angle * PIdiv180)),
						GL_Vector.multiply(UpVector, (float) Math.sin(Angle * PIdiv180))
				));
		
		//now compute the new UpVector (by cross product)
		UpVector = GL_Vector.multiply(GL_Vector.crossProduct(ViewDir, RightVector), -1);
	}
	
	/**
	 * Rotate the camera around the absolute vertical axis (0,1,0), NOT around the cameras Y axis.
	 * This simulates a person looking up or down and rotating in place.  You will rotate your 
	 * body around the vertical axis, while your head remains tilted at the same angle.  
	 * @param Angle the angle to rotate around the vertical axis in degrees
	 */
	public static void RotateV(float Angle) {
		// Make a matrix to rotate the given number of degrees around Y axis
		GL_Matrix M = GL_Matrix.rotateMatrix(0,(float)Math.toRadians(Angle),0);
		// rotate the view vector 
		GL_Vector vd = M.transform(ViewDir);
		// the up vector is perpendicular to the old view and the new view
		UpVector = (Angle > 0)? GL_Vector.crossProduct(ViewDir,vd) : GL_Vector.crossProduct(vd,ViewDir);
		// the right vector is perpendicular to the new view and Up vectors
		RightVector = GL_Vector.crossProduct(vd,UpVector);
		// set the view direction
		ViewDir = vd;
		RotatedY += Angle;
	}
	
	public static void MoveForward(float Distance) {
		Position = GL_Vector.add(Position, GL_Vector.multiply(ViewDir, -Distance));
	}
	
	public static void MoveUpward(float Distance) {
		Position = GL_Vector.add(Position, GL_Vector.multiply(UpVector, Distance));
	}
	
	public static void StrafeRight(float Distance) {
		Position = GL_Vector.add(Position, GL_Vector.multiply(RightVector, Distance));
	}
	
	/**
	 * Call GLU.gluLookAt() to set view position, direction and orientation.  Be 
	 * sure that the modelview matrix is current before calling Render() 
	 * (glMatrixMode(GL_MODEL_VIEW)).
	 */
	public static void Render() {
		//The point at which the camera looks:
		GL_Vector ViewPoint = GL_Vector.add(Position, ViewDir);
		
		//as we know the up vector, we can easily use gluLookAt:
		GLU.gluLookAt(Position.x, Position.y, Position.z,
				ViewPoint.x, ViewPoint.y, ViewPoint.z,
				UpVector.x, UpVector.y, UpVector.z);
		
		//System.out.println(Position.x + "," + Position.y + "," + Position.z + "  " + 
		//		ViewDir.x + "," + ViewDir.y + "," + ViewDir.z + "  " + 
		//		UpVector.x + "," + UpVector.y + "," + UpVector.z + "  " 
		//		);
	}
	
	/** 
	 * Return the current camera view direction
	 */
	public static GL_Vector getViewDir() {
		return ViewDir;
	}
	
}

