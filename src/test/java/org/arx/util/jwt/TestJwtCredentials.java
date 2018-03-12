package org.arx.util.jwt;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import org.arx.Resource;
import org.arx.util.Configuration;
import org.arx.util.SimpleResource;
import org.arx.util.jwt.JwtCredentials;
import org.junit.Before;
import org.junit.Test;

public class TestJwtCredentials {
	
	@Before
	public void init() {
		Configuration.createInstance("src/test/resources");
	}

	@Test
	public void testJwtToken() throws UnsupportedEncodingException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, InvalidKeySpecException, NoSuchAlgorithmException {
		// token equals to : "# r, internal/# -"
		String token = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIHIsIGludGVybmFsLyMgLSJ9.A45usQSkL6QsImi95AQvYz2940bZyF4kwh2Ay83pN_Ji2cSh_xd2JANrJoiPON4qZNHmjQLfcZPEyewhqx9LmADKyl82LN25TQmX_maBvaRLf7pSU4VQFiT2hU4ztIZWsfTa9DQyWaU7a5y4tHxMIpa664DBlHCKLybdAMH1Wl4";
		JwtCredentials sc = new JwtCredentials(token);
		Map<Resource,String> entries = sc.getEntries();
		assertEquals(2,entries.size());
		assertEquals("r",entries.get(new SimpleResource("#")));
		assertEquals("-",entries.get(new SimpleResource("internal/#")));
	}
	
	@Test
	public void testSerialize() throws IllegalArgumentException, SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		String token = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJyZ3MiOiIjIHIsIGludGVybmFsLyMgLSJ9.A45usQSkL6QsImi95AQvYz2940bZyF4kwh2Ay83pN_Ji2cSh_xd2JANrJoiPON4qZNHmjQLfcZPEyewhqx9LmADKyl82LN25TQmX_maBvaRLf7pSU4VQFiT2hU4ztIZWsfTa9DQyWaU7a5y4tHxMIpa664DBlHCKLybdAMH1Wl4";
		JwtCredentials sc = new JwtCredentials(token);
		assertEquals("Bearer " + token,sc.serialize());
	}
	
	@Test(expected = TokenExpiredException.class)
	public void testExpiredToken() throws UnsupportedEncodingException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, InvalidKeySpecException, NoSuchAlgorithmException {
		// token equals to : "# r, internal/# -" with expiration time 2018-03-07 09:35:13 CET
		String token = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MjA0MTE3MTMsInJncyI6IiMgciwgaW50ZXJuYWwvIyAtIn0.OnqNRNK3X7ztwfmdcc_mT7CsvSdka5MmpfxdT5vSAOYLhwJp-STSyYtmzfMDMizE8oKnghZsy87qb2iFNhsHpRJW2FffR1uMtFNFBc2zkLUYdCF4UYRS6CNC39lISVp6GFKzDqNCuCnb8x-CTSIUdXpKknzBB67RUIJBe_CRkHM";
		new JwtCredentials(token);
	}

	@Test(expected = JWTDecodeException.class)
	public void testInvalidToken() throws UnsupportedEncodingException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, InvalidKeySpecException, NoSuchAlgorithmException {
		// token is invalid
		String token = "yJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MjA0MTE3MTMsInJncyI6IiMgciwgaW50ZXJuYWwvIyAtIn0.OnqNRNK3X7ztwfmdcc_mT7CsvSdka5MmpfxdT5vSAOYLhwJp-STSyYtmzfMDMizE8oKnghZsy87qb2iFNhsHpRJW2FffR1uMtFNFBc2zkLUYdCF4UYRS6CNC39lISVp6GFKzDqNCuCnb8x-CTSIUdXpKknzBB67RUIJBe_CRkHM";
		new JwtCredentials(token);
	}

	@Test(expected = SignatureVerificationException.class)
	public void testInvalidSignature() throws UnsupportedEncodingException, IllegalArgumentException, SignatureVerificationException, TokenExpiredException, InvalidKeySpecException, NoSuchAlgorithmException {
		// token signature is invalid
		String token = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MjA0MTE3MTMsInJncyI6IiMgciwgaW50ZXJuYWwvIyAtIn0.OnqNRNK3X7ztwfmdcc_mT7CsvSdka5MmpfxdT5vSAOYLhwJp-STSyYtmzfMDMizE8oKnghZsy87qb2iFNhsHpRJW2FffR1uMtFNFBc2zkLUYdCF4UYRS6CNC39lISVp6GFKzDqNCuCnb8x-CTSIUdXpKknzBB67RUIJBe_CRkH";
		new JwtCredentials(token);
	}

}
