package com.programmerdan.minecraft.wordbank.util;

import com.programmerdan.minecraft.wordbank.CharConfig;
import com.programmerdan.minecraft.wordbank.WordBank;

/**
 * Utility to build a name. Optionally marks it as "used" in the data store.
 * @author ProgrammerDan
 *
 */
public class NameConstructor {
	
	/**
	 * Constructing a name is a several step process.
	 * 
	 * First, the color is computed.
	 * Then, the number of words is computed.
	 * Finally, each word is computed.
	 * All these parts are joined and returned.
	 * 
	 * @param key The character sequence used to construct a WordBank name.
	 * @param mark If true, save key as used; otherwise, do not.
	 * @return The converted key.
	 */
	public static String buildName(String key, boolean mark) {
		// TODO: add mark storage; mark is ignored for now, tbd.
		
		// First, compute color.
		float whichColor = executeConfig(WordBank.config().getColor(), key);
		// Second, compute # of words.
		float howManyWords = executeConfig(WordBank.config().getWordCount(), key);
		
		int actualWords = Math.max(1, (int) (howManyWords * WordBank.config().getWordMax()));
		
		StringBuilder name = new StringBuilder();
		name.append(org.bukkit.ChatColor.getByChar(Integer.toString((int)(15 * whichColor), 16)));
		for (int nWord = 0; nWord < actualWords; nWord++) {
			if (nWord > 0) { 
				name.append(" ");
			}
			name.append(WordBank.config().getWords().getWord(
					executeConfig(WordBank.config().getWordConfig(nWord), key)
				));
		}
		
		return name.toString();
	}
	
	public static float executeConfig(CharConfig conf, String key) {
		Character[] extract = new Character[conf.charIndex.length];
		int extractIdx = 0;
		for (int idx : conf.charIndex) {
			extract[extractIdx++] = (idx >= 0 && idx <= key.length() ? key.charAt(idx) : '0');
		}
		
		return conf.charFunction.process(extract, conf.functionParams);
	}
}
