package org.arx;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestMessageType {
	
	@Test
	public void testGetCode() {
		assertEquals(MessageType.PING_CODE,MessageType.PING.getCode());
		assertEquals(MessageType.CREATE_CODE,MessageType.CREATE.getCode());
		assertEquals(MessageType.UPDATE_CODE,MessageType.UPDATE.getCode());
		assertEquals(MessageType.SAVE_CODE,MessageType.SAVE.getCode());
		assertEquals(MessageType.DELETE_CODE,MessageType.DELETE.getCode());
		assertEquals(MessageType.READ_CODE,MessageType.READ.getCode());
		assertEquals(MessageType.SUBSCRIBE_CODE,MessageType.SUBSCRIBE.getCode());
		assertEquals(MessageType.SUBSCRIBE_STATUS_CODE,MessageType.SUBSCRIBE_STATUS.getCode());
		assertEquals(MessageType.UNSUBSCRIBE_CODE,MessageType.UNSUBSCRIBE.getCode());
		assertEquals(MessageType.UNSUBSCRIBE_ALL_CODE,MessageType.UNSUBSCRIBE_ALL.getCode());
		assertEquals(MessageType.SUCCESS_CODE,MessageType.SUCCESS.getCode());
		assertEquals(MessageType.DATA_CODE,MessageType.DATA.getCode());
		assertEquals(MessageType.BAD_REQUEST_CODE,MessageType.BAD_REQUEST.getCode());
		assertEquals(MessageType.FORBIDDEN_CODE,MessageType.FORBIDDEN.getCode());
		assertEquals(MessageType.NOT_FOUND_CODE,MessageType.NOT_FOUND.getCode());
		assertEquals(MessageType.ALREADY_EXISTS_CODE,MessageType.ALREADY_EXISTS.getCode());
		assertEquals(MessageType.INTERNAL_SERVER_ERROR_CODE,MessageType.INTERNAL_SERVER_ERROR.getCode());
		assertEquals(MessageType.OUT_OF_SYNC_CODE,MessageType.OUT_OF_SYNC.getCode());
	}
	
	@Test
	public void testIsRequest() {
		assertEquals(true,MessageType.PING.isRequest());
		assertEquals(true,MessageType.CREATE.isRequest());
		assertEquals(true,MessageType.UPDATE.isRequest());
		assertEquals(true,MessageType.SAVE.isRequest());
		assertEquals(true,MessageType.DELETE.isRequest());
		assertEquals(true,MessageType.READ.isRequest());
		assertEquals(true,MessageType.SUBSCRIBE.isRequest());
		assertEquals(true,MessageType.SUBSCRIBE_STATUS.isRequest());
		assertEquals(true,MessageType.UNSUBSCRIBE.isRequest());
		assertEquals(true,MessageType.UNSUBSCRIBE_ALL.isRequest());
		assertEquals(false,MessageType.SUCCESS.isRequest());
		assertEquals(false,MessageType.DATA.isRequest());
		assertEquals(false,MessageType.BAD_REQUEST.isRequest());
		assertEquals(false,MessageType.FORBIDDEN.isRequest());
		assertEquals(false,MessageType.NOT_FOUND.isRequest());
		assertEquals(false,MessageType.ALREADY_EXISTS.isRequest());
		assertEquals(false,MessageType.INTERNAL_SERVER_ERROR.isRequest());
		assertEquals(false,MessageType.OUT_OF_SYNC.isRequest());
	}

	@Test
	public void testIsResponse() {
		assertEquals(false,MessageType.PING.isResponse());
		assertEquals(false,MessageType.CREATE.isResponse());
		assertEquals(false,MessageType.UPDATE.isResponse());
		assertEquals(false,MessageType.SAVE.isResponse());
		assertEquals(false,MessageType.DELETE.isResponse());
		assertEquals(false,MessageType.READ.isResponse());
		assertEquals(false,MessageType.SUBSCRIBE.isResponse());
		assertEquals(false,MessageType.SUBSCRIBE_STATUS.isResponse());
		assertEquals(false,MessageType.UNSUBSCRIBE.isResponse());
		assertEquals(false,MessageType.UNSUBSCRIBE_ALL.isResponse());
		assertEquals(true,MessageType.SUCCESS.isResponse());
		assertEquals(true,MessageType.DATA.isResponse());
		assertEquals(true,MessageType.BAD_REQUEST.isResponse());
		assertEquals(true,MessageType.FORBIDDEN.isResponse());
		assertEquals(true,MessageType.NOT_FOUND.isResponse());
		assertEquals(true,MessageType.ALREADY_EXISTS.isResponse());
		assertEquals(true,MessageType.INTERNAL_SERVER_ERROR.isResponse());
		assertEquals(true,MessageType.OUT_OF_SYNC.isResponse());
	}

	@Test
	public void testIsError() {
		assertEquals(false,MessageType.PING.isError());
		assertEquals(false,MessageType.CREATE.isError());
		assertEquals(false,MessageType.UPDATE.isError());
		assertEquals(false,MessageType.SAVE.isError());
		assertEquals(false,MessageType.DELETE.isError());
		assertEquals(false,MessageType.READ.isError());
		assertEquals(false,MessageType.SUBSCRIBE.isError());
		assertEquals(false,MessageType.SUBSCRIBE_STATUS.isError());
		assertEquals(false,MessageType.UNSUBSCRIBE.isError());
		assertEquals(false,MessageType.UNSUBSCRIBE_ALL.isError());
		assertEquals(false,MessageType.SUCCESS.isError());
		assertEquals(false,MessageType.DATA.isError());
		assertEquals(true,MessageType.BAD_REQUEST.isError());
		assertEquals(true,MessageType.FORBIDDEN.isError());
		assertEquals(true,MessageType.NOT_FOUND.isError());
		assertEquals(true,MessageType.ALREADY_EXISTS.isError());
		assertEquals(true,MessageType.INTERNAL_SERVER_ERROR.isError());
		assertEquals(true,MessageType.OUT_OF_SYNC.isError());
	}
	
	@Test
	public void testValueOf() {
		for ( MessageType type : MessageType.values() ) {
			assertEquals(type,MessageType.valueOf(type.getCode()));
		}
	}
}