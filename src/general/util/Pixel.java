package general.util;

import general.GeneralGLWorldLine;

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.util.vector.Vector4f;

public class Pixel {
	
	/////////////// Private Class Fields
	
	Manifold manifold;
	HashMap<Integer, Tensor> tensorMap;
	int[] position;
	
	static final int DIMENSION = Tensor.DIMENSION;
	
	public static final int METRIC = 0;
	public static final int STRESS_ENERGY = 1;
	public static final int INV_METRIC = 2;
	public static final int METRIC_CONNECTION = 3;
	public static final int RIEMANN = 4;
	public static final int EINSTEIN = 5;
	public static final int CHRISTOFFEL = 6;
	
	ArrayList<GeneralGLWorldLine> thesePoints;

	public Pixel(Manifold mani, int[] newPos) {
		manifold = mani;
		if(newPos.length != DIMENSION) {
			System.err.println("ERROR: Position must be " + DIMENSION + " dimensional!");
			System.exit(-1);
		}
		position = newPos.clone();
		tensorMap = new HashMap<Integer, Tensor>();
		tensorMap.put(METRIC, Manifold.EUCLIDEAN_METRIC);
		tensorMap.put(STRESS_ENERGY, Manifold.EUCLIDEAN_METRIC);
		tensorMap.put(INV_METRIC, Manifold.EUCLIDEAN_METRIC);
		tensorMap.put(METRIC_CONNECTION, Manifold.NULL_TENSOR);
	}
	
	public Tensor differentiate(Integer whichTensor) {
		Tensor retTensor;
		Tensor inTensor = tensorMap.get(whichTensor);
		
		// Construct new tensor or proper rank.
		// The first rank is the derivative-added rank
		boolean retTensorTypes[] = new boolean[inTensor.getRankArray().length+1];
		retTensorTypes[0] = Tensor.COVARIANT;
		boolean inTensorTypes[] = inTensor.getRankArray();
		for(int i=0; i<inTensorTypes.length; i++) {
			retTensorTypes[i+1] = inTensorTypes[i];
		}
		retTensor = new Tensor(retTensorTypes);
		
		// Fill in new Tensor numerically from adjacent tensors.
		Manifold m = this.getManifold();
		
		// Iterate through each dimension (0,1,2,3 is t,x,y,z)
		for(int i=0; i<DIMENSION; i++) {
			
			// Get Adjacent Tensors along axis
			Tensor[] adjTensors = new Tensor[] 
					{m.getAdjPositions(this.position, i)[0].getTensor(whichTensor), 
					m.getAdjPositions(this.position, i)[1].getTensor(whichTensor)};
			
			// Average differences
			double[] newValues;
			
			// Check for boundary conditions
			switch(m.isBoundary(position, i)) {
			case 1:
				newValues = inTensor.subtract(adjTensors[0]).getRaw();
				break;
			case -1:
				newValues = adjTensors[1].subtract(inTensor).getRaw();
				break;
			default:
				newValues = adjTensors[1].subtract(inTensor).add(
						inTensor.subtract(adjTensors[0])).getScale(0.5).getRaw();
				break;
			}
			
			// Assign new values to return tensor
			for(int ii=0; ii<newValues.length; ii++) {
				retTensor.setRaw(ii + i*newValues.length, newValues[ii]);
			}
			
		}
		
		return retTensor;
	}
	
	public void CalculateConnection() {
		Tensor inv_metric = getInvMetric();
		Tensor dmetric = differentiate(Pixel.METRIC);
		
		Tensor connection_first = inv_metric.contract(1, dmetric, 1);
		Tensor connection_second = inv_metric.contract(1, dmetric.getSwap(0, 2), 1);
		Tensor connection_third = inv_metric.contract(1, dmetric, 0);
		tensorMap.put(Pixel.METRIC_CONNECTION, 
				connection_first.add(connection_second).subtract(connection_third).getScale(0.5));
	}
	
	public void Einstein() {
		Tensor dconnection = differentiate(Pixel.METRIC_CONNECTION);
		Tensor connection = getTensor(Pixel.METRIC_CONNECTION);
		Tensor riemann1 = dconnection.getSwap(0, 1).getSwap(1, 2);
		Tensor riemann2 = dconnection.getSwap(0,1).getSwap(1,2).getSwap(2,3);
		Tensor riemann3 = connection.contract(0, connection, 1).getSwap(0,2).getSwap(1,2).getSwap(2,3);
		Tensor riemann4 = connection.contract(0, connection, 1).getSwap(0,2).getSwap(1,2);
		tensorMap.put(Pixel.RIEMANN, 
				riemann1.subtract(riemann2).add(riemann3).subtract(riemann4));
		Tensor riemann = tensorMap.get(Pixel.RIEMANN);
		
		Tensor ricciTensor = riemann.contractSelf(0, 2);
		Tensor invMetric = tensorMap.get(Pixel.INV_METRIC);
		Tensor ricciScalar = invMetric.contract(0, ricciTensor, 0).contractSelf(0, 1);
		tensorMap.put(Pixel.EINSTEIN, 
				ricciTensor.subtract(tensorMap.get(Pixel.METRIC).getScale(ricciScalar.get()/2.0)).
				contract(0, invMetric, 1).contract(1, invMetric, 1)
				);
	}
	
	public void Christoffel() {
		Tensor inv_metric = getInvMetric();
		Tensor dmetric = differentiate(Pixel.METRIC);
		
		Tensor christoffel_first = inv_metric.contract(1, dmetric, 0);
		Tensor christoffel_second = christoffel_first.getSwap(1, 2);
		Tensor christoffel_third = inv_metric.contract(1, dmetric, 2);
		
		tensorMap.put(Pixel.CHRISTOFFEL, 
				christoffel_first.add(christoffel_second).subtract(christoffel_third).getScale(0.5));
		
	}
	
	public void calculateStressEnergy() {
		//TODO: Ask Dr. Dong about Switching to Covariant Tensor
		Tensor newStress = Manifold.NULL_TENSOR;
		for(GeneralGLWorldLine wl : thesePoints) {
			Tensor velocity = wl.getVelocity().getScale(1.0/wl.getVelocity().get(0));
			double beta = velocity.get(2)*velocity.get(2) + velocity.get(1)*velocity.get(1)
					+ velocity.get(3)*velocity.get(3);
			if(beta > 1) {
				System.err.println("ERROR! Object moving faster than light!");
				System.exit(-1);
			}
			Tensor thisTensor = new Tensor(2, 0, Tensor.CONTRAVARIANT);
			
			// Set Diagonals
			thisTensor.set(velocity.get(0)*velocity.get(0), 0,0);
			thisTensor.set(velocity.get(1)*velocity.get(1), 1,1);
			thisTensor.set(velocity.get(2)*velocity.get(2), 2,2);
			thisTensor.set(velocity.get(3)*velocity.get(3), 3,3);
			
			
			thisTensor.set(velocity.get(1)*velocity.get(0), 1,0);
			thisTensor.set(velocity.get(1)*velocity.get(0), 0,1);
			
			thisTensor.set(velocity.get(2)*velocity.get(0), 2,0);
			thisTensor.set(velocity.get(2)*velocity.get(0), 0,2);
			
			thisTensor.set(velocity.get(3)*velocity.get(0), 0,3);
			thisTensor.set(velocity.get(3)*velocity.get(0), 3,0);
			
			
			thisTensor.set(velocity.get(2)*velocity.get(1), 1,2);
			thisTensor.set(velocity.get(2)*velocity.get(1), 2,1);
			
			thisTensor.set(velocity.get(3)*velocity.get(1), 3,1);
			thisTensor.set(velocity.get(3)*velocity.get(1), 1,3);
			
			
			thisTensor.set(velocity.get(3)*velocity.get(2), 3,2);
			thisTensor.set(velocity.get(3)*velocity.get(2), 2,3);
			
			thisTensor.scale(1.0/Math.sqrt(1-beta));
			newStress = newStress.add(thisTensor);
		}
		this.setStressEnergy(newStress);
	}
	
	//////////// Getters and Setters

	public Manifold getManifold() {
		return manifold;
	}

	public void setManifold(Manifold manifold) {
		this.manifold = manifold;
	}

	public int[] getPosition() {
		return position;
	}

	public void setPosition(int[] position) {
		this.position = position;
	}
	
	public Tensor getTensor(Integer newTensor) {
		return tensorMap.get(newTensor);
	}
	
	public Tensor getMetric() {
		return tensorMap.get(METRIC);
	}
	
	public Tensor getInvMetric() {
		return tensorMap.get(INV_METRIC);
	}
	
	public Tensor getStressEnergy() {
		return tensorMap.get(STRESS_ENERGY);
	}
	
	public void setTensor(Integer newIndex, Tensor newTensor) {
		tensorMap.put(newIndex, newTensor);
	}
	
	public void setMetric(Tensor newTensor) {
		tensorMap.put(METRIC, newTensor);
		tensorMap.put(INV_METRIC, invert(newTensor));
	}
	
	public void setStressEnergy(Tensor newTensor) {
		tensorMap.put(STRESS_ENERGY, newTensor);
	}
	
	//////////////// End Getters And Setters
	
	//////////////// Helper Functions
	
	private Tensor invert(Tensor metric) {
		double[] tmp = new double[12];
	    double[] src = new double[16];
	    double[] dst = new double[16];  

	    // Transpose matrix
	    for (int i = 0; i < 4; i++) {
	      src[i +  0] = metric.getRaw()[i*4 + 0];
	      src[i +  4] = metric.getRaw()[i*4 + 1];
	      src[i +  8] = metric.getRaw()[i*4 + 2];
	      src[i + 12] = metric.getRaw()[i*4 + 3];
	    }

	    // Calculate pairs for first 8 elements (cofactors) 
	    tmp[0] = src[10] * src[15];
	    tmp[1] = src[11] * src[14];
	    tmp[2] = src[9]  * src[15];
	    tmp[3] = src[11] * src[13];
	    tmp[4] = src[9]  * src[14];
	    tmp[5] = src[10] * src[13];
	    tmp[6] = src[8]  * src[15];
	    tmp[7] = src[11] * src[12];
	    tmp[8] = src[8]  * src[14];
	    tmp[9] = src[10] * src[12];
	    tmp[10] = src[8] * src[13];
	    tmp[11] = src[9] * src[12];
	    
	    // Calculate first 8 elements (cofactors)
	    dst[0]  = tmp[0]*src[5] + tmp[3]*src[6] + tmp[4]*src[7];
	    dst[0] -= tmp[1]*src[5] + tmp[2]*src[6] + tmp[5]*src[7];
	    dst[1]  = tmp[1]*src[4] + tmp[6]*src[6] + tmp[9]*src[7];
	    dst[1] -= tmp[0]*src[4] + tmp[7]*src[6] + tmp[8]*src[7];
	    dst[2]  = tmp[2]*src[4] + tmp[7]*src[5] + tmp[10]*src[7];
	    dst[2] -= tmp[3]*src[4] + tmp[6]*src[5] + tmp[11]*src[7];
	    dst[3]  = tmp[5]*src[4] + tmp[8]*src[5] + tmp[11]*src[6];
	    dst[3] -= tmp[4]*src[4] + tmp[9]*src[5] + tmp[10]*src[6];
	    dst[4]  = tmp[1]*src[1] + tmp[2]*src[2] + tmp[5]*src[3];
	    dst[4] -= tmp[0]*src[1] + tmp[3]*src[2] + tmp[4]*src[3];
	    dst[5]  = tmp[0]*src[0] + tmp[7]*src[2] + tmp[8]*src[3];
	    dst[5] -= tmp[1]*src[0] + tmp[6]*src[2] + tmp[9]*src[3];
	    dst[6]  = tmp[3]*src[0] + tmp[6]*src[1] + tmp[11]*src[3];
	    dst[6] -= tmp[2]*src[0] + tmp[7]*src[1] + tmp[10]*src[3];
	    dst[7]  = tmp[4]*src[0] + tmp[9]*src[1] + tmp[10]*src[2];
	    dst[7] -= tmp[5]*src[0] + tmp[8]*src[1] + tmp[11]*src[2];
	    
	    // Calculate pairs for second 8 elements (cofactors)
	    tmp[0]  = src[2]*src[7];
	    tmp[1]  = src[3]*src[6];
	    tmp[2]  = src[1]*src[7];
	    tmp[3]  = src[3]*src[5];
	    tmp[4]  = src[1]*src[6];
	    tmp[5]  = src[2]*src[5];
	    tmp[6]  = src[0]*src[7];
	    tmp[7]  = src[3]*src[4];
	    tmp[8]  = src[0]*src[6];
	    tmp[9]  = src[2]*src[4];
	    tmp[10] = src[0]*src[5];
	    tmp[11] = src[1]*src[4];

	    // Calculate second 8 elements (cofactors)
	    dst[8]   = tmp[0] * src[13]  + tmp[3] * src[14]  + tmp[4] * src[15];
	    dst[8]  -= tmp[1] * src[13]  + tmp[2] * src[14]  + tmp[5] * src[15];
	    dst[9]   = tmp[1] * src[12]  + tmp[6] * src[14]  + tmp[9] * src[15];
	    dst[9]  -= tmp[0] * src[12]  + tmp[7] * src[14]  + tmp[8] * src[15];
	    dst[10]  = tmp[2] * src[12]  + tmp[7] * src[13]  + tmp[10]* src[15];
	    dst[10] -= tmp[3] * src[12]  + tmp[6] * src[13]  + tmp[11]* src[15];
	    dst[11]  = tmp[5] * src[12]  + tmp[8] * src[13]  + tmp[11]* src[14];
	    dst[11] -= tmp[4] * src[12]  + tmp[9] * src[13]  + tmp[10]* src[14];
	    dst[12]  = tmp[2] * src[10]  + tmp[5] * src[11]  + tmp[1] * src[9];
	    dst[12] -= tmp[4] * src[11]  + tmp[0] * src[9]   + tmp[3] * src[10];
	    dst[13]  = tmp[8] * src[11]  + tmp[0] * src[8]   + tmp[7] * src[10];
	    dst[13] -= tmp[6] * src[10]  + tmp[9] * src[11]  + tmp[1] * src[8];
	    dst[14]  = tmp[6] * src[9]   + tmp[11]* src[11]  + tmp[3] * src[8];
	    dst[14] -= tmp[10]* src[11 ] + tmp[2] * src[8]   + tmp[7] * src[9];
	    dst[15]  = tmp[10]* src[10]  + tmp[4] * src[8]   + tmp[9] * src[9];
	    dst[15] -= tmp[8] * src[9]   + tmp[11]* src[10]  + tmp[5] * src[8];

	    // Calculate determinant
	    double det = src[0]*dst[0] + src[1]*dst[1] + src[2]*dst[2] + src[3]*dst[3];
	    
	    // Calculate matrix inverse
	    det = 1.0 / det;
	    Tensor retTensor = new Tensor(2,0,Tensor.CONTRAVARIANT);
	    for (int i = 0; i < 16; i++)
	      retTensor.set(dst[i] * det, i%4, i/4);
	    return retTensor;
	}
	
	
	//////////////// Point Stuff
	
	public void addPoint(GeneralGLWorldLine wl) {
		thesePoints.add(wl);
	}
	
	public void updatePoints() {
		for(GeneralGLWorldLine wl : thesePoints) {
			Tensor acceleration = this.getTensor(Pixel.CHRISTOFFEL).contract(1, wl.getVelocity(), 0).contract(1, wl.getVelocity(), 0);
			acceleration.scale(-1);
			Vector4f vectorAcceleration = new Vector4f(
					(float)acceleration.get(1),
					(float)acceleration.get(2),
					(float)acceleration.get(3),
					(float)acceleration.get(0)
					);
			wl.update(vectorAcceleration);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void passPoints() {
		ArrayList<GeneralGLWorldLine> temp = (ArrayList<GeneralGLWorldLine>) thesePoints.clone();
		for(GeneralGLWorldLine wl : temp) {
			thesePoints.remove(wl);
			this.manifold.addPoint(wl);
		}
	}
	
	public ArrayList<GeneralGLWorldLine> getAllPoints() {
		return thesePoints;
	}
}
