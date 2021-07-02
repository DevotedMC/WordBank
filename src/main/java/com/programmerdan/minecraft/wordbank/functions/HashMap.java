package com.programmerdan.minecraft.wordbank.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import com.programmerdan.minecraft.wordbank.CharFunction;
import com.programmerdan.minecraft.wordbank.WordBank;

/**
 * Aggregates the bytes of the characters used as input and forms a message digest using those
 * bytes.
 * 
 * @author ProgrammerDan
 *
 */
public class HashMap extends CharFunction {
	
	public HashMap() {
		super(null);
	}
	
	public HashMap(WordBank plugin) {
		super(plugin);
	}

	/**
	 * Thin wrapper for {@link #process(Character[], String)}. 
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
	 * Maps the input characters onto a byte map. 
	 * 
	 * @param input the Characters to map.
	 * @return a number between 0.0f and 1.0f
	 */
	public float process(Character[] input, String param) {
		if (input == null || input.length == 0) {
			throw new IllegalArgumentException("At least one character must be passed in.");
		}
		
		Vector<Byte> inputs = new Vector<>();
		for (Character c : input) {
			byte[] b = c.toString().getBytes();
			
			for (byte z : b){
				inputs.add(z);
			}
		}
		
		byte[] arrz = ArrayUtils.toPrimitive(inputs.toArray(new Byte[0]));
		
		try {
			MessageDigest md = MessageDigest.getInstance(param);
			byte[] outc = md.digest(arrz);
			
			byte[] arrc = new byte[outc.length];
			Arrays.fill(arrc, (byte) 0xFF);
			BigInteger max = new BigInteger(1, arrc);
			BigInteger val = new BigInteger(1, outc);
			BigDecimal maxD = new BigDecimal(max);
			BigDecimal valD = new BigDecimal(val);

			float q = valD.divide(maxD, 10, RoundingMode.HALF_UP).floatValue();
			/*if (plugin().config().isDebug()) plugin().log(Level.INFO,"Hash: {0} / {1} = {2}",
					valD.toPlainString(), maxD.toPlainString(), q);*/
			return q;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Failed to instantiate cryptographic function " + param, e);
		}
	}
}
