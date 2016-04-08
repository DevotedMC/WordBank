package com.programmerdan.minecraft.wordbank.test;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WordBankTestUtil {
	private static Logger logger = Logger.getLogger("WordBankTest");
	private static ArrayList<Character[]> sampleData = null;

	public static ArrayList<Character[]> getTestKeys() {
		if (sampleData == null) {
			generateTestKeys();
		} 
		return sampleData;
	}

	public static void generateTestKeys() {
		logger.info("Generating Test Keys");
		int len = 10; // TODO draw from config
		int count = 50000;
		char min = ' ';
		char max = '~';
		Random rnd = new Random();
		sampleData = new ArrayList<Character[]>();
		for (int i = 0; i < count; i++) {
			Character[] n = new Character[len];
			for (int j = 0; j < len; j++) {
				n[j] = (char) (min + rnd.nextInt(max-min+1));
			}
			sampleData.add(n);
		}
		logger.log(Level.INFO, "Generated {0} test keys", count);
	}

}
