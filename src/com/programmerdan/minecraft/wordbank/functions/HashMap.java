package com.programmerdan.minecraft.wordbank.functions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import com.programmerdan.minecraft.wordbank.CharFunction;

/**
 * Aggregates the bytes of the characters used as input and forms a message digest using those
 * bytes.
 * 
 * @author ProgrammerDan
 *
 */
public class HashMap implements CharFunction {

	/**
	 * Thin wrapper for {@link #process(String[])}. 
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
		
		byte[] arrz = ArrayUtils.toPrimitive((Byte[]) inputs.toArray());
		
		try {
			MessageDigest md = MessageDigest.getInstance(param);
			byte[] outc = md.digest(arrz);
			
			byte[] arrc = new byte[outc.length];
			Arrays.fill(arrc, (byte) 0xFF);
			BigInteger max = new BigInteger(arrc);
			max.clearBit(0); // force positive
			BigInteger val = new BigInteger(outc);
			val.clearBit(0); // force positive
			BigDecimal maxD = new BigDecimal(max);
			BigDecimal valD = new BigDecimal(val);
			return valD.divide(maxD).floatValue();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Failed to instantiate cryptographic function " + param, e);
		}
	}
}
