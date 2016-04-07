package com.programmerdan.minecraft.wordbank.test;

import java.util.ArrayList;
import java.util.Random;

public class WordBankTestUtil {
	private static ArrayList<Character[]> sampleData = null;

	public static ArrayList<Character[]> getTestKeys() {
		if (sampleData == null) {
			generateTestKeys();
		} 
		return sampleData;
	}

	public static void generateTestKeys() {
		int len = 10; // TODO draw from config
		int count = 70000;
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
	}

}
