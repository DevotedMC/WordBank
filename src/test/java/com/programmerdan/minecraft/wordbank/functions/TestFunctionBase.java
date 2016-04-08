package com.programmerdan.minecraft.wordbank.functions;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.programmerdan.minecraft.wordbank.WordBank;
import com.programmerdan.minecraft.wordbank.WordBankConfig;

public class TestFunctionBase {

	protected static Logger logger = Logger.getLogger("WordBankTest");
	protected static final WordBank wb = mock(WordBank.class);
	protected static final WordBankConfig wbc = mock(WordBankConfig.class);

	@BeforeClass
	public static void setup() {
		logger.log(Level.INFO, "Setting up mocked WordBank config");
		when(wbc.isDebug()).thenReturn(false);
		when(wb.config()).thenReturn(wbc);
		when(wb.logger()).thenReturn(logger);
		doNothing().when(wb).log(null, null, (Object)null);
	}

	@AfterClass
	public static void tearDown() {
		logger.log(Level.INFO, "Destroying mocked WordBank config");
		System.gc();
	}

	public TestFunctionBase() {
		super();
	}

	@After
	public void postTest() {
		System.gc();
	}
	
	public void log(Level level, String message, Object...objects) {
		logger.log(level, message, objects);
	}

}