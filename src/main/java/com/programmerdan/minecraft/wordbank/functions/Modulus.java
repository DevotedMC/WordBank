package com.programmerdan.minecraft.wordbank.functions;

import java.security.InvalidParameterException;
import java.util.logging.Level;

import com.programmerdan.minecraft.wordbank.CharFunction;
import com.programmerdan.minecraft.wordbank.WordBank;

/**
 * Uses an aggregator and modulo to form a value between 0.0f and 1.0f
 * 
 * @author ProgrammerDan
 *
 */
public class Modulus implements CharFunction {

	/**
	 * Thin wrapper for {@link #process(String[], Integer)}. 
	 * See it for details.
	 */
	public float process(Character[] input, Object...params) {
		if (params == null || params.length == 0) {
			throw new InvalidParameterException("At least one parameter was expected.");
		}
		if (params[0] instanceof Integer) {
			return process(input, (Integer) params[0]);
		} else {
			throw new InvalidParameterException("The first parameter must be an Integer.");
		}
	}
	
	/**
	 * This one is a little strange. Takes the bytes of the character input, aggregates them
	 * in reconstructed bit order, and takes the modulo to base {@param mod} at each pass. This
	 * ensures that the final number is between 0 and {@param mod}. Consequently, the result is
	 * mapped between 0.0f and 1.0f by dividing the result over {@param mod}.
	 * 
	 * @param input The characters to use in the mapping
	 * @param mod The modulo to suppress by
	 * 
	 * @return a number between 0.0f and 1.0f 
	 */
	public float process(Character[] input, Integer mod) {
		if (input == null || input.length == 0) {
			throw new InvalidParameterException("At least one character must be passed in.");
		}
		
		if (mod <= 0) {
			throw new InvalidParameterException("Modulo needs to be greater then 0.");
		}
		
		int aggregator = 0;
		
		for (Character c : input) {
			byte[] b = c.toString().getBytes();
			for (int a = 0; a < b.length; a++) {
				aggregator |= (b[a] << (8*a)); 
			}
			if (WordBank.config().isDebug()) WordBank.log().log(Level.INFO,"Modulo: {0} % {1}",
					new Object[]{aggregator, mod});
			aggregator %= mod;
		}
		
		float q = (float) aggregator / (float) (mod - 1);
		
		if (WordBank.config().isDebug()) WordBank.log().log(Level.INFO,"Modulo: {0} / {1} = {2}",
				new Object[]{aggregator, mod, q});
		
		return q;
		
	}

}
