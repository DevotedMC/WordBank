package com.programmerdan.minecraft.wordbank;

/**
 * Thin wrapper for underlying function, {@link #process(Character[], Object...)}.
 * 
 * @author ProgrammerDan
 */
public abstract class CharFunction {
	private WordBank plugin = null;
	public CharFunction(WordBank plugin) {
		this.plugin = plugin;
	}
	
	protected WordBank plugin() {
		return this.plugin == null ? WordBank.instance() : this.plugin; 
	}
	
	/**
	 * Converts character input and static params into a number between 0 and 1 that can be
	 * used by the program internally to produce an output.
	 * 
	 * @param input The characters extracted from a larger input that this function should process 
	 * @param params Any standard parameters needed by this CharFunction. Check the contract of
	 * 		the specific implementing class when deciding what to pass.
	 * @return A number between 0.0f and 1.0f.
	 */
	public abstract float process(Character[] input, Object...params);
}
