package org.komparator.security;

import org.junit.*;
import static org.junit.Assert.*;

public class CryptoUtilTest {

    // static members
	private static final String VALID_CC = "1234567890123452";
	private static String ENCRYPTED_CC="";

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() {
        // runs once before all tests in the suite
    }

    @AfterClass
    public static void oneTimeTearDown() {
        // runs once after all tests in the suite
    }

    // members

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    	ENCRYPTED_CC=CryptoUtil.cifer(VALID_CC);
    }

    @After
    public void tearDown() {
        // runs after each test
    }

    // tests
    @Test
    public void testCifer() {
    	assertFalse(ENCRYPTED_CC.equals(VALID_CC));
    }
    @Test
    public void testDecifer() {
        
        assertTrue(CryptoUtil.decifer(ENCRYPTED_CC).equals(VALID_CC));
    }

}
