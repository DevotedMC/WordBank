package com.programmerdan.minecraft.wordbank;

/**
 * Thin wrapper for underlying function, {@link #process(String[], Object...)}.
 * 
 * @author ProgrammerDan
 */
public interface CharFunction {
	/**
	 * Converts character input and static params into a number between 0 and 1 that can be
	 * used by the program internally to produce an output.
	 * 
	 * @param input The characters extracted from a larger input that this function should process 
	 * @param params Any standard parameters needed by this CharFunction. Check the contract of
	 * 		the specific implementing class when deciding what to pass.
	 * @return A number between 0.0f and 1.0f.
	 */
	public float process(String[] input, Object...params);
}
