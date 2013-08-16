package general.util;

import java.util.Random;

public class Tensor {
	
	/**
	 * Rank N Tensor Class for use with Einstein Notation.
	 * @auther Ethan Kroll Gordon
	 */

	////////////////////// Class Fields

	// True = Contravariant
	// False = Covariant
	boolean contra_covariant[];
	int rank;

	public static final int DIMENSION = 4;
	double elementData[];
	
	int swapArr[];
	
	public static final boolean CONTRAVARIANT = true;
	public static final boolean COVARIANT = false;

	////////////////////// End Class Fields

	///////////////////// Constructors

	/**
	 * Create a new Tensor with an array of rank definitions.
	 * 
	 * Convention:
	 * 	Tensor.CONTRAVARIANT = true
	 * 	Tensor.COVARIANT = false
	 * 
	 * The rank of the resulting tensor is the size of the array.
	 * An empty array (or no arguments) can be passed for a Scalar.
	 * 
	 * @param ccarray Array of booleans corresponding to each rank.
	 */
	public Tensor(boolean... ccarray) {
		swapArr = new int[rank];
		for(int i=0; i<rank; i++) swapArr[i] = i;
		rank = ccarray.length;
		contra_covariant = ccarray;
		elementData = new double[(int) Math.pow(DIMENSION, ccarray.length)];
	}

	/**
	 * Shortcut to create a new tensor with non-intermixed covariant and contravariant ranks.
	 * 
	 * This only works if all covariant ranks and all contravariant ranks are consecutive.
	 * 
	 * @param contravariant Number of consecutive contravariant ranks.
	 * @param covariant Number of consecutive covariant ranks.
	 * @param which_is_first Which type of rank should come first in the definition.
	 */
	public Tensor(int contravariant, int covariant, boolean which_is_first) {
		contra_covariant = new boolean[contravariant + covariant];
		if (which_is_first) {
			for (int i = 0; i < contravariant; i++)
				contra_covariant[i] = true;
			for (int i = contravariant; i < contravariant + covariant; i++)
				contra_covariant[i] = false;
		} else {
			for (int i = 0; i < covariant; i++)
				contra_covariant[i] = false;
			for (int i = covariant; i < contravariant + covariant; i++)
				contra_covariant[i] = true;
		}
		rank = contra_covariant.length;
		elementData = new double[(int) Math.pow(DIMENSION, rank)];
	}

	///////////////////// End Constructors

	//////////////////// Basic Operations

	/**
	 * Set the value at a specific index of the Tensor.
	 * 
	 * @param value The value to be put into the Tensor.
	 * @param numbers The index to put the number at. Must be the same size as the rank of the Tensor.
	 */
	public void set(double value, int... numbers) {
		int getNumbers[] = numbers.clone();
		for(int i=0; i<numbers.length; i++) {
			numbers[i] = getNumbers[swapArr[i]];
		}
		if (numbers.length != rank) {
			System.err.println("ERROR: Input of " + numbers.length
					+ " does not match Tensor Rank " + rank + ".");
			return;
		} else {
			int index = 0;
			for (int i = 0; i < numbers.length; i++) {
				if (numbers[i] < 0 || numbers[i] > DIMENSION)
					throw new IndexOutOfBoundsException(
							String.valueOf(numbers[i]));
				index += numbers[i] * Math.pow(DIMENSION, i);
			}
			elementData[index] = value;
		}
	}

	/**
	 * Get the current value at a specific index of the Tensor
	 * .
	 * @param numbers The index to get the value from. Must be the same size as the rank of the Tensor.
	 * @return Double value at the specific index.
	 */
	public double get(int... numbers) {
		int getNumbers[] = numbers.clone();
		for(int i=0; i<numbers.length; i++) {
			numbers[i] = getNumbers[swapArr[i]];
		}
		if (numbers.length != rank) {
			System.err.println("ERROR: Input of " + numbers.length
					+ " does not match Tensor Rank " + rank + ".");
			return 0.0;
		} else {
			int index = 0;
			for (int i = 0; i < numbers.length; i++) {
				if (numbers[i] < 0 || numbers[i] > DIMENSION)
					throw new IndexOutOfBoundsException(
							String.valueOf(numbers[i]));
				index += numbers[i] * Math.pow(DIMENSION, i);
			}
			return elementData[index];
		}
	}

	///////////////// End Basic Operations

	/////////////////// Get Rank Operations
	
	/**
	 * Get boolean representation of the rank type.
	 * 
	 * Convention:
	 * 	Tensor.CONTRAVARIANT = true
	 * 	Tensor.COVARIANT = false
	 * 
	 * @param rank Which rank to investigate.
	 * @return Boolean value coresponding to rank type.
	 */
	public boolean getRankType(int rank) {
		return contra_covariant[rank];
	}
	
	/**
	 * Number of ranks in this Tensor.
	 * @return See Description
	 */
	public int getRank() {
		return rank;
	}
	
	/**
	 * Gets the raw rank array for this Tensor. Mostly for debugging purposes.
	 * @return See Description
	 */
	public boolean[] getRankArray() {
		return this.contra_covariant;
	}
	
	/**
	 * Get the raw data array from the tensor.
	 * This occasionally makes for easier computation.
	 * 
	 * @return Raw double array of the tensor. Size = DIMENSION**Rank
	 */
	public double[] getRaw() {
		return elementData;
	}
	
	/**
	 * Set the raw element of the elementArray for this tensor.
	 * @param index Index of the value to set.
	 * @param value Input data value.
	 */
	public void setRaw(int index, double value) {
		elementData[index] = value;
	}
	
	/**
	 * @return Raw size of element value array.
	 */
	public int getRawSize() {
		return elementData.length;
	}

	/////////////////// End Get Rank Operations

	/////////////////// Combination Functions

	/**
	 * 
	 * Contracts two Tensors by summing over one Contravariant and one Covariant Rank.
	 * 
	 * The result is a new Tensor with a rank equal to this rank plus the other rank minus 2 
	 * (1 rank from each Tensor being contracted).
	 * 
	 * @param thisRank The index between 0 and this.getRank() for the rank of this Tensor to contract.
	 * @param other The other Tensor to contract with.
	 * @param otherRank The index between 0 and other.getRank() for the rank of this Tensor to contract.
	 * @return A new Tensor formed after the contraction.
	 */
	public Tensor contract(int thisRank, Tensor other, int otherRank) {
		if (this.getRankType(thisRank) == other.getRankType(otherRank)) {
			System.out
					.println("ERROR: Must contract covariant with contravariant rank!");
			return null;
		} else {
			// Construct New Tensor with Correct Ranks
			boolean ccarray[] = new boolean[this.getRank() + other.getRank()
					- 2];
			for (int i = 0; i < this.getRank(); i++) {
				if (i == thisRank)
					continue;
				else
					ccarray[i] = this.getRankType(i);
			}
			for (int i = 0; i < other.getRank(); i++) {
				if (i == otherRank)
					continue;
				else
					ccarray[this.getRank() + i] = other.getRankType(i);
			}
			Tensor returnTensor = new Tensor(ccarray);

			// Fill in new Tensor
			int setNumbers[] = new int[returnTensor.getRank()];
			int thisNumbers[] = new int[this.getRank()];
			int otherNumbers[] = new int[other.getRank()];

			int array_offset = 0;

			for (int i = 0; i < Math.pow(DIMENSION, setNumbers.length); i++) {

				// Set Up Array of Integers to specify index of new Tensor
				for (int ii = 0; ii < setNumbers.length; ii++) {
					setNumbers[ii] = (i / (int) Math.pow(DIMENSION, ii))
							% DIMENSION;
				}

				// Start Summation
				double thisValue = 0;
				double otherValue = 0;
				double newValue = thisValue * otherValue;
				double sum = 0.0;

				// Sum from 0 to DIMENSION
				for (int ii = 0; ii < DIMENSION; ii++) {

					// Get index from first Tensor
					array_offset = 0;
					for (int iii = 0; iii < thisNumbers.length; iii++) {
						if (iii == thisRank) {
							array_offset++;
							continue;
						} else {
							thisNumbers[iii] = setNumbers[ii - array_offset];
						}
					}
					thisNumbers[thisRank] = ii;

					// Get value from first Tensor
					thisValue = this.get(thisNumbers);

					// Get index from other Tensor
					array_offset = this.getRank()-1;
					for (int iii = 0; iii < otherNumbers.length; iii++) {
						if (iii == thisRank) {
							array_offset++;
							continue;
						} else {
							otherNumbers[iii] = setNumbers[ii - array_offset];
						}
					}
					otherNumbers[otherRank] = ii;

					// Get value from other Tensor
					otherValue = other.get(otherNumbers);

					// Multiply and add to Summation
					newValue = otherValue * thisValue;
					sum += newValue;

				} // End of Summation

				// Add Sum to Return Tensor
				returnTensor.set(sum, setNumbers);
			}

			// Return the new Tensor
			return returnTensor;
		} // End else block
	} // End contract()
	
	/**
	 * Contracts one Tensor by summing over one Contravariant and one Covariant Rank.
	 * 
	 * The result is a new Tensor with a rank equal to this rank minus 2 
	 * (1 Contravariant and one Covariant rank being contracted).
	 * 
	 * @param firstRank The index between 0 and getRank of the first rank to contract.
	 * @param secondRank The index between 0 and getRank of the second rank to contract. Cannot be the same type as firstRank
	 * @return A new Tensor formed after the contraction.
	 */
	public Tensor contractSelf(int firstRank, int secondRank) {
		if (this.getRankType(firstRank) == this.getRankType(secondRank)) {
			System.out
					.println("ERROR: Must contract covariant with contravariant rank!");
			return null;
		} else {
			
			// Create new tensor of correct rank
			boolean[] rankType = new boolean[this.getRank()-2];
			int array_offset = 0;
			for(int i=0; i<rankType.length; i++) {
				if(i == firstRank || i == secondRank) {
					array_offset++;
				}
				rankType[i] = this.getRankArray()[i+array_offset];
			}
			
			Tensor returnTensor = new Tensor(rankType);
			
			// Indices of the new Tensor and values from this tensor
			int setNumbers[] = new int[returnTensor.getRank()];
			int firstNumbers[] = new int[this.getRank()];
			
			// Loop through all possibilities for the indices
			for (int i = 0; i < Math.pow(DIMENSION, setNumbers.length); i++) {

				// Set Up Array of Integers to specify index of new Tensor
				for (int ii = 0; ii < setNumbers.length; ii++) {
					setNumbers[ii] = (i / (int) Math.pow(DIMENSION, ii))
							% DIMENSION;
				}
				
				// Set Up Array of 
				
				// Set up summation
				double sum = 0.0;
				
				// Loop over all indices of this Tensor
				for(int ii=0; ii<DIMENSION; ii++) {
					
					// Get index for value
					array_offset = 0;
					for (int iii = 0; iii < firstNumbers.length; iii++) {
						if (iii == firstRank || iii == secondRank) {
							array_offset++;
							continue;
						} else {
							firstNumbers[iii] = setNumbers[ii - array_offset];
						}
					}
					// Fill in summation index
					firstNumbers[firstRank] = ii;
					firstNumbers[secondRank] = ii;
					
					// Add to sum
					sum += this.get(firstNumbers);
				} // End Sum For Loop
				
				// Add Sum to new Tensor
				returnTensor.set(sum, setNumbers);
				
			} // End New Tensor For Loop
			
			// Return new Tensor
			return returnTensor;
			
		} // End else block
	} // End contractSelf()

	
	/**
	 * Adds two different Tensors of the same rank as one would add two euclidean vectors.
	 * 
	 * @param other Other Tensor to add. Must have the same Rank Array as this Tensor.
	 * @return The resulting Tensor of the same rank.
	 */
	public Tensor add(Tensor other) {

		// Start with sanity checks
		if (other.getRank() != this.getRank()) {
			System.err.println("ERROR: You an only add tensors of the same rank!");
			return null;
		} else {
			// Another Sanity Check
			for (int i = 0; i < this.getRank(); i++) {
				if (this.getRankType(i) != other.getRankType(i)) {
					System.err
							.println("ERROR: You an only add tensors of the same rank configuration!");
					return null;
				}
			}

			// Construct tensor of the correct rank
			Tensor returnTensor = new Tensor(this.getRankArray());

			// Loop though every value in each tensor
			int setNumbers[] = new int[returnTensor.getRank()];
			for (int i = 0; i < Math.pow(DIMENSION, setNumbers.length); i++) {

				// Set Up Array of Integers to specify index of new Tensor
				for (int ii = 0; ii < setNumbers.length; ii++) {
					setNumbers[ii] = (i / (int) Math.pow(DIMENSION, ii))
							% DIMENSION;
				}

				// Set the value
				returnTensor.set(this.get(setNumbers) + other.get(setNumbers),
						setNumbers);
			}

			// Return the new Tensor
			return returnTensor;

		} // End else block
	} // End add()

	
	/**
	 * Shortcut to adding the opposite of the Tensor parameter.
	 * @see this.add()
	 * 
	 * @param other
	 * @return
	 */
	public Tensor subtract(Tensor other) {
		Tensor negTensor = null;
		negTensor = (Tensor) other.clone();

		// Loop though every value in each tensor
		int setNumbers[] = new int[other.getRank()];
		for (int i = 0; i < Math.pow(DIMENSION, setNumbers.length); i++) {

			// Set Up Array of Integers to specify index of new Tensor
			for (int ii = 0; ii < setNumbers.length; ii++) {
				setNumbers[ii] = (i / (int) Math.pow(DIMENSION, ii))
						% DIMENSION;
			}

			// Reverse the value in the tensor
			negTensor.set(-1*other.get(setNumbers), setNumbers);
		}
		
		// Add the opposite Tensor to subtract
		return this.add(negTensor);
		
	} // End subtract

	/////////////////////// End Combination Functions
	
	/////////////////// Mutation Functions
	
	/**
	 * Scale all tensor values by one number.
	 * @param scalar Double that every value is multiplied by.
	 */
	public void scale(double scalar) {
		for(int i=0; i<elementData.length; i++) {
			elementData[i] *= scalar;
		}
	}
	
	public Tensor getScale(double scalar) {
		Tensor retTensor = this.clone();
		retTensor.scale(scalar);
		return retTensor;
	}
	
	public void swap(Integer rank1, Integer rank2) {
		Integer temp = swapArr[rank1];
		swapArr[rank1] = swapArr[rank2];
		swapArr[rank2] = temp;
		boolean tempB = contra_covariant[rank1];
		contra_covariant[rank1] = contra_covariant[rank2];
		contra_covariant[rank2] = tempB;
	}
	
	public Tensor getSwap(Integer rank1, Integer rank2) {
		Tensor retTensor = this.clone();
		retTensor.swap(rank1, rank2);
		return retTensor;
	}
	
	public Tensor clone() {
		Tensor retTensor = new Tensor(this.getRankArray());
		double[] newValues = this.getRaw();
		for(int i=0; i<newValues.length; i++) {
			retTensor.setRaw(i, newValues[i]);
		}
		return retTensor;
	}
	
	/////////////////// End Mutation Functions
	
	public static Tensor GenerateRandomMetric(Tensor metric, double range) {
		Random newRandom = new Random();
		Tensor retTensor = metric.clone();
		int i=0;
		for(double d : retTensor.getRaw()) {
			retTensor.setRaw(i, d + newRandom.nextDouble()*range-(range/2));
			i++;
		}
		
		// Removing some parameters
		retTensor.set(1, 0,0);
		for(int ii=0; ii<4; ii++) {
			retTensor.set(0, 0,ii);
		}
		
		// Reflect metric
		retTensor.set(retTensor.get(0,1), 1,0);
		retTensor.set(retTensor.get(0,2), 2,0);
		retTensor.set(retTensor.get(0,3), 3,0);
		
		retTensor.set(retTensor.get(1,2), 2,1);
		retTensor.set(retTensor.get(1,3), 3,1);
		
		retTensor.set(retTensor.get(2,3), 3,2);
		
		return retTensor;
	}

} // End Class Tensor
