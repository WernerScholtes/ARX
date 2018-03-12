package org.arx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestReason {
	@Test
	public void testGetCode() {
		assertEquals(Reason.INITIAL_CODE,Reason.INITIAL.getCode());
		assertEquals(Reason.CREATED_CODE,Reason.CREATED.getCode());
		assertEquals(Reason.UPDATED_CODE,Reason.UPDATED.getCode());
		assertEquals(Reason.DELETED_CODE,Reason.DELETED.getCode());
	}

	@Test
	public void testValueOf() {
		for ( Reason reason : Reason.values() ) {
			assertEquals(reason,Reason.valueOf(reason.getCode()));
		}
	}
}
