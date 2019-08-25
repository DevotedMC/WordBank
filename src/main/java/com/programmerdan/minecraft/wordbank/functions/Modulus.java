package com.programmerdan.minecraft.wordbank.functions;

import java.util.logging.Level;

import com.programmerdan.minecraft.wordbank.CharFunction;
import com.programmerdan.minecraft.wordbank.WordBank;

/**
 * Uses an aggregator and modulo to form a value between 0.0f and 1.0f
 * 
 * @author ProgrammerDan
 *
 */
public class Modulus extends CharFunction {

	public Modulus() {
		super(null);
	}

	public Modulus(WordBank plugin) {
		super(plugin);
	}

	/**
	 * Thin wrapper for {@link #process(Character[], Integer)}. 
	 * See it for details.
	 */
	public float process(Character[] input, Object...params) {
		if (params == null || params.length == 0) {
			throw new IllegalArgumentException("At least one parameter was expected.");
		}
		if (params[0] instanceof Integer) {
			return process(input, (Integer) params[0]);
		} else {
			throw new IllegalArgumentException("The first parameter must be an Integer.");
		}
	}
	
	/**
	 * This one is a little strange. Takes the bytes of the character input, aggregates them
	 * in reconstructed bit order, and takes the modulo to base {@code mod} at each pass. This
	 * ensures that the final number is between 0 and {@code mod}. Consequently, the result is
	 * mapped between 0.0f and 1.0f by dividing the result over {@code mod}.
	 * 
	 * @param input The characters to use in the mapping
	 * @param mod The modulo to suppress by
	 * 
	 * @return a number between 0.0f and 1.0f 
	 */
	public float process(final Character[] input, final Integer mod) {
		if (input == null || input.length == 0) {
			throw new IllegalArgumentException("At least one character must be passed in.");
		}
		
		if (mod <= 0) {
			throw new IllegalArgumentException("Modulo needs to be greater then 0.");
		}
		
		int aggregator = 0;
		
		for (Character c : input) {
			byte[] b = String.valueOf(c).getBytes();
			for (int a = 0; a < b.length; a++) {
				aggregator |= (b[a] << (8*a)); 
			}
			aggregator %= mod;
			b = null;
		}
		
		float q = (float) aggregator / (float) (mod - 1);
		
		//if (plugin().config().isDebug()) plugin().log(Level.INFO,"Modulo: {0} / {1} = {2}", aggregator, mod, q);
		
		return q;
		
	}

}
