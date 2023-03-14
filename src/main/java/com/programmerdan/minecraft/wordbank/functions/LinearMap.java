package com.programmerdan.minecraft.wordbank.functions;

import com.programmerdan.minecraft.wordbank.CharFunction;
import com.programmerdan.minecraft.wordbank.WordBank;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;

/**
 * Fairly naive process that maps the character data onto a number whose bitlength depends on the
 * number of input characters; then that mapped number is divided by the maximum size of the number
 * to produce an outcome between 0.0f and 1.0f.
 * 
 * @author ProgrammerDan
 *
 */
public class LinearMap extends CharFunction {

	public LinearMap() {
		super(null);
	}
	
	public LinearMap(WordBank plugin) {
		super(plugin);
	}

	/**
	 * Thin wrapper for {@link #process(Character[])}. 
	 * See it for details.
	 */
	public float process(Character[] input, Object...params) {
		return process(input);
	}

	/**
	 * Maps the input characters onto a bitmap of length 16*n where input's length is n. Then
	 * divides the numeric representation of that map by the maximum value of the bitmap.
	 * 
	 * @param input the Characters to map.
	 * @return a number between 0.0f and 1.0f
	 */
	public float process(Character[] input) {
		if (input == null || input.length == 0) {
			throw new InvalidParameterException("At least one character must be passed in.");
		}
		
		Vector<Byte> inputs = new Vector<>();
		for (Character c : input) {
			byte[] b = c.toString().getBytes();
			byte[] b2 = new byte[2];
			b2[0] = b[0];
			if (b.length > 1) {
				b2[1] = b[b.length];
				if (b.length > 2) {
					b2[0] |= (byte) (0x00 ^ b[2]); // invert
					b2[1] |= (byte) (0x00 ^ b[2]);
				} // any additional bytes are ignored
			} else {
				b2[1] = (byte) (0x00 ^ b[0]);
			}
			inputs.add(b2[0]);
			inputs.add(b2[1]);
		}
		
		byte[] arrz = new byte[inputs.size()];
		Arrays.fill(arrz, (byte) 0xFF);
		BigInteger max = new BigInteger(1, arrz);
		//max.clearBit(0); // force positive
		BigInteger val = new BigInteger(1, ArrayUtils.toPrimitive(inputs.toArray(new Byte[0])));
		//val.clearBit(0); // force positive
		BigDecimal maxD = new BigDecimal(max);
		BigDecimal valD = new BigDecimal(val);
		
		float q = valD.divide(maxD, 10, RoundingMode.HALF_EVEN).floatValue();
		if (plugin().config().isDebug()) plugin().logger().log(Level.INFO,"Linear: {0} / {1} = {2}",
				new Object[]{valD.toPlainString(), maxD.toPlainString(), q});
		return q;
	}
}
