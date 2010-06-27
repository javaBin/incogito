package no.java.incogito.web.servlet;

import org.joda.time.LocalDate;
import org.junit.Test;

import junit.framework.TestCase;


public class IncogitoFunctionsTest extends TestCase{
	
	
	@Test
	public void testShouldFormatDate() {
		String date = IncogitoFunctions.formatDate(new LocalDate(2010, 6, 27));
		assertEquals("Sun 27 June", date);
		
		date = IncogitoFunctions.formatDate(new LocalDate(2010, 9, 9));
		assertEquals("Thu 9 September", date);
		
	}

}
