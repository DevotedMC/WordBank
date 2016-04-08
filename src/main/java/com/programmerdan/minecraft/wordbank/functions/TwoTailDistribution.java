package com.programmerdan.minecraft.wordbank.functions;

import com.programmerdan.minecraft.wordbank.CharFunction;
import com.programmerdan.minecraft.wordbank.WordBank;

public class TwoTailDistribution extends CharFunction {

	public TwoTailDistribution() {
		super(null);
	}
	
	public TwoTailDistribution(WordBank plugin) {
		super(plugin);
	}

	/**
	 * Thin wrapper for {@link #process(Character[], Integer, Integer)}. 
	 * See it for details.
	 */
	public float process(Character[] input, Object...params) {
		if (params == null || params.length == 0) {
			throw new IllegalArgumentException("At least one parameter was expected.");
		}
		if (params[0] instanceof String) {
			return process(input, (String) params[0]);
		} else {
			throw new IllegalArgumentException("The first parameter must be an String.");
		}
	}

	/**
	 * Filter an assumed uniform input into a two tailed (high on bottom and top) output, with no skew.
	 * 
	 * @param input Character sequence, first passed through HashMap function.
	 * @param param The Hashing function to use.
	 * @return a value between 0 and 1, that tends to be near 0.5 
	 */
	public float process(Character[] input, String param) {
		double q = (double) new HashMap().process(input, param);
		
		// one option///
		//y = ( e ^ (-.5 * ( ( x - .5 ) /.1675 ) ^ 2 ) )
		// see: https://www.wolframalpha.com/input/?i=y+%3D+(+e+%5E+(-.5+*+(+(+x+-+.5+)+%2F.1675+)+%5E+2+)+)+,+x+%3D+0...1
		//double outcome = Math.exp( -0.5d * Math.pow(( q - .5d ) / 0.1675d, 2.0d ) );
		
		// actual: (approximate integral of above)
		//y = 1 / (1+e^(-(x-.5)*10))
		// see: http://www.wolframalpha.com/input/?i=y+%3D+1+%2F+(1%2Be%5E(-(x-.5)*10))+and+x%3D0...1
		double outcome = 1.0d / (1.0d + Math.exp( -10.0d * (q - 0.5d) ) );
		
		/*if (plugin().config().isDebug()) plugin().logger().log(Level.INFO,"NormalDistro: {0} = {1}",
				new Object[]{q, outcome});*/
		
		return (float) (outcome < 0.0d ? 0.0f : (outcome > 1.0d ? 1.0f : outcome));
	}

}
