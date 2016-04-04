package com.programmerdan.minecraft.wordbank;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.InvalidPluginException;

/**
 * Wraps the configuration for a "Character based functional interpreter", or CharFunction.
 * Describes the indexes of the characters to extract, and any default parameters that should
 * be passed to the underlying function object.
 * 
 * See contract of {@link CharFunction} for details on that interface.
 * 
 * @author ProgrammerDan
 *
 */
public class CharConfig {
	public int[] charIndex;
	public CharFunction charFunction;
	public Object[] functionParams;
	
	public CharConfig(ConfigurationSection config, int activation_limit) throws InvalidPluginException{
		List<Integer> charIndexList = config.getIntegerList("chars");
		if (charIndexList == null || charIndexList.size() == 0) {
			throw new InvalidPluginException("Invalid character configuration");
		}
		
		this.charIndex = new int[charIndexList.size()];
		
		for (int a = 0; a < charIndexList.size(); a++) {
			this.charIndex[a] = charIndexList.get(a);
			if (this.charIndex[a] > activation_limit) {
				throw new InvalidPluginException("Invalid character configuration, index out of range");
			}
		}
		
		try {
			Class<?> clz = Class.forName(config.getString("function"));
			this.charFunction = (CharFunction) clz.newInstance();
		} catch(ClassCastException cce) {
			throw new InvalidPluginException("Invalid character configuration, function is not a CharFunction", cce);
		} catch (ClassNotFoundException e) {
			throw new InvalidPluginException("Invalid character configuration, char function class not found", e);
		} catch (InstantiationException e) {
			throw new InvalidPluginException("Invalid character configuration, char function uninstantiated", e);
		} catch (IllegalAccessException e) {
			throw new InvalidPluginException("Invalid character configuration, cannot access constructor", e);
		}
		
		try {
			List<?> terms = config.getList("function_terms");
			if (terms == null || terms.size() == 0) {
				functionParams = new Object[0];
			} else {
				functionParams = terms.toArray();
			}
		} catch (NullPointerException npe) {
			throw new InvalidPluginException("Unable to determine function parameters");
		}
	}
}
