package general.util;

import general.GeneralGLWorldLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Manifold {
	
	HashMap<String, Pixel> maniMap;
	
	Integer size;
	
	public static Tensor EUCLIDEAN_METRIC = null;
	public static Tensor NULL_TENSOR = null;
	
	static final int DIMENSION = Tensor.DIMENSION;
	public static final int RESOLUTION = 10; // Amount of pixels in each Pixel
	
	static {
		// Identity Matrix
		EUCLIDEAN_METRIC = new Tensor(0,2,Tensor.COVARIANT);
		NULL_TENSOR = new Tensor(0,2,Tensor.COVARIANT);
		for(int i=0; i<4; i++) {
			for(int ii=0; ii<4; ii++) {
				if(i == ii) {
					EUCLIDEAN_METRIC.set(1, i, ii);
				} else {
					EUCLIDEAN_METRIC.set(0, i, ii);
				}
				NULL_TENSOR.set(0,i,ii);
			}
		}
	}
	
	public Manifold(int newSize) {
		this.size = newSize;
		if(this.size%2 == 0) {
			System.err.println("WARNING: Manifold size wil be made odd!");
			this.size++;
		}
		maniMap = new HashMap<String, Pixel>((int)Math.pow(size, 4));
		int[] index = new int[4];
		for(int i=-size/2; i<=size/2; i++) {
			for(int ii=-size/2; ii<=size/2; ii++) {
				for(int iii=-size/2; iii<=size/2; iii++) {
					for(int iiii=-size/2; iiii<=size/2; iiii++) {
						index[0] = i; index[1] = ii; index[2] = iii; index[3] = iiii;
						maniMap.put(Arrays.toString(index), new Pixel(this, index));
						maniMap.get(Arrays.toString(index)).setMetric(EUCLIDEAN_METRIC);
						maniMap.get(Arrays.toString(index)).setStressEnergy(NULL_TENSOR);
					}
				}
			}
		}
	}
	
	/**
	 * 
     *               WRITE
     *                           ,,
     *                          ';;
     *                           ''
     *             ____          ||
     *            ;    \         ||
     *             \,---'-,-,    ||
     *             /     (  o)   ||
     *           (o )__,--'-' \  ||
     * ,,,,       ;'uuuuu''   ) ;;
     * \   \      \ )      ) /\//
     *  '--'       \'nnnnn' /  \
     *    \\      //'------'    \
     *     \\    //  \           \
     *      \\  //    )           )
     *       \\//     |           |
     *        \\     /            |
     *        
     *           ALL THE GETTERS! 
     *             (And Setters)
	 **/
	
	public Pixel get(int t, int x, int y, int z) {
		return maniMap.get(Arrays.toString(new int[] {t,x,y,z}));
	}
	
	public Pixel get(int[] arr) {
		return this.get(arr[0], arr[1], arr[2], arr[3]);
	}
	
	public Tensor getMetric(int t, int x, int y, int z) {
		return this.get(t, x, y, z).getMetric();
	}
	
	public Tensor getMetric(int[] arr) {
		return this.get(arr[0], arr[1], arr[2], arr[3]).getMetric();
	}
	
	public Tensor getStressEnergy(int t, int x, int y, int z) {
		return this.get(t, x, y, z).getStressEnergy();
	}
	
	public Tensor getStressEnergy(int[] arr) {
		return this.get(arr[0], arr[1], arr[2], arr[3]).getStressEnergy();
	}
	
	public int getSize() {
		return size;
	}
	
	public HashMap<String, Pixel> getPixelMap() {
		return maniMap;
	}
	
	public Pixel[] getAdjPositions(int[] pos, int axis) {
		if(axis >= DIMENSION || pos.length != DIMENSION) {
			System.err.println("WARNING: Wrong dimension on request for adjacent positions!");
			return null;
		} else {
			Pixel[] retPixels = new Pixel[2];
			for(int i=0; i<DIMENSION; i++) {
				if(i == axis) pos[i]--;
			}
			if(isBoundary(pos, axis) < 0) retPixels[0] = null;
			else retPixels[0] = this.get(pos);
			for(int i=0; i<DIMENSION; i++) {
				if(i == axis) pos[i]+=2;
			}
			if(isBoundary(pos, axis) > 0) retPixels[1] = null;
			else retPixels[1] = this.get(pos);
			return retPixels;
		}
	}
	
	/**
	 * Determines if a given position is on the boundary of the Manifold to prevent IndexOutOfBounds Errors.
	 * @param pos Position to investigate.
	 * @param axis Axis to test for boundary conditions.
	 * @return 1 if on Upper Boundary, -1 if on Lower Boundary, 0 Otherwise
	 */
	public int isBoundary(int[] pos, int axis) {
		if(pos[axis] == -size/2) return -1;
		else if(pos[axis] == size/2) return 1;
		else return 0;
	}
	
	public void calculateMetric(int passes, int numRandom) {
		recalculateAllEinsteins();
		for(int i=0; i<passes; i++) {
			for(Pixel px : maniMap.values()) {
				double chai_squared = getChaiSquared(px.getTensor(Pixel.EINSTEIN), px.getTensor(Pixel.STRESS_ENERGY).getScale(8*Math.PI));
				double new_chai = 0;
				boolean newBest = true;
				Tensor bestTensor = px.getTensor(Pixel.METRIC);
				while(newBest) {
					newBest = false;
					for(int ii=0; ii<numRandom; ii++) {
						Tensor t = Tensor.GenerateRandomMetric(bestTensor, chai_squared);
						px.setMetric(t);
						px.CalculateConnection();
						px.Einstein();
						new_chai = getChaiSquared(px.getTensor(Pixel.EINSTEIN), px.getTensor(Pixel.STRESS_ENERGY).getScale(8*Math.PI));
						if(new_chai < chai_squared) {
							chai_squared = new_chai;
							newBest = true;
							bestTensor = t;
						}
					}
				}
				px.setMetric(bestTensor);
			}
		}
	}
	
	public void recalculateAllEinsteins() {
		for(Pixel px : maniMap.values()) {
			px.CalculateConnection();
		}
		for(Pixel px : maniMap.values()) {
			px.Einstein();
		}
	}
	
	public void recalculateAllEnergies() {
		for(Pixel px : maniMap.values()) {
			px.calculateStressEnergy();
		}
	}
	
	public void addPoint(GeneralGLWorldLine pt) {
		Tensor velocity = pt.getVelocity();
		Pixel px = this.get(
				(int)velocity.get(0)/10, 
				(int)velocity.get(1)/10, 
				(int)velocity.get(2)/10, 
				(int)velocity.get(3)/10
				);
		px.addPoint(pt);
	}
	
	private double getChaiSquared(Tensor test, Tensor target) {
		if(test.getRank() != target.getRank()) return Double.MAX_VALUE;
		for(int i=0; i<test.getRank(); i++) {
			if(test.getRankArray()[i] != target.getRankArray()[i]) return Double.MAX_VALUE;
		}
		double returnVal = 0;
		for(int i=0; i<test.getRaw().length; i++) {
			returnVal += (test.getRaw()[i] - target.getRaw()[i])*(test.getRaw()[i] - target.getRaw()[i]);
		}
		return returnVal;
	}
	
	public ArrayList<GeneralGLWorldLine> getAllPoints() {
		ArrayList<GeneralGLWorldLine> ret = new ArrayList<GeneralGLWorldLine>();
		for(Pixel px : maniMap.values()) {
			ret.addAll(px.getAllPoints());
		}
		return ret;
	}
	
	public void recalculateAllChristoffels() {
		for(Pixel px : maniMap.values()) {
			px.Christoffel();
		}
	}
	
	public void recalculateAllPoints() {
		for(Pixel px : maniMap.values()) {
			px.updatePoints();
		}
		for(Pixel px : maniMap.values()) {
			px.passPoints();
		}
	}
	
}
