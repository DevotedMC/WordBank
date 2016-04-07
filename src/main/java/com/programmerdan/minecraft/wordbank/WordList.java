package com.programmerdan.minecraft.wordbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;

/**
 * The core word list for this plugin. Assumes that the input is a single word per line.
 * 
 * @author ProgrammerDan
 *
 */
public class WordList {
	private Vector<String> words;
	
	public WordList(InputStream words) {
		this.words = new Vector<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(words));
			String word = br.readLine();
			while (word != null) {
				this.words.add(word);
				word = br.readLine();
			}
		} catch (IOException ioe) {
			WordBank.log().log(Level.SEVERE, "Failed to load word list!", ioe);
		}
	}
	
	public String getWord(float which) {
		if (which > 1.0f) which = 1.0f;
		if (which < 0.0f) which = 0.0f;
		int q = (int) ((float)(words.size()-1) * which);
		return words.get(q);
	}
}
