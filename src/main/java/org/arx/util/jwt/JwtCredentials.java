package org.arx.util.jwt;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.arx.Credentials;
import org.arx.Resource;
import org.arx.util.Configuration;
import org.arx.util.StringCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * A Javascript Web Token (JWT) based implementation of the Credentials
 * interface. The JWT claim "rgs" is supposed to contain an authorization string
 * as specified in {@link org.arx.util.StringCredentials}.
 * <p>
 * In order to be able to verify the JWT token, the public key must be stored in
 * the file $ARX_HOME/conf/public.key as a Base64 encoded string.
 */
public class JwtCredentials implements Credentials {
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtCredentials.class);
	private static final String JWT_FORMAT = "Bearer %1$s";
	private static RSAPublicKey publicKey;
	private StringCredentials credentials;
	private String token;
	private String decodedToken;
	private long expirationTime;

	/**
	 * Creates a credentials object from a coded JWT token.
	 * 
	 * @param token
	 *            the coded JWT token to be used
	 * @throws IllegalArgumentException
	 *             if the decoded token does not contain a valid authorization
	 *             string
	 * @throws SignatureVerificationException
	 *             if the signature of the token cannot be verified
	 * @throws TokenExpiredException
	 *             if the token has expired
	 * @throws JWTDecodeException
	 *             if the token cannot be decoded
	 * @throws NoSuchAlgorithmException
	 *             if the specified algorithm for verification cannot be found
	 */
	public JwtCredentials(String token) throws IllegalArgumentException, SignatureVerificationException,
			TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		decodeAndParseJwt(token);
	}

	/**
	 * Returns the coded JWT token
	 * 
	 * @return the coded JWT token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Returns the decoded JWT token
	 * 
	 * @return the decoded JWT token
	 */
	public String getDecodedToken() {
		return decodedToken;
	}

	/**
	 * Returns the expiration time in milliseconds since midnight, January 1
	 * 1970 UTC
	 * 
	 * @return the expiration time in milliseconds since midnight, January 1
	 *         1970 UTC or 0L, if this credentials has no expiration time
	 */
	public long getExpirationTime() {
		return expirationTime;
	}

	/**
	 * Decodes and parses a JSON Web Token (JWT) for an authorization string.
	 * After decoding of the specified token, the claim "rgs" is read from the
	 * token. The string value of this claim is then parsed as authorization
	 * string (see
	 * {@link org.arx.util.StringCredentials#parseAuthorization(String,long)
	 * StringCredentials.parseAuthorization(authorization,expirationTime)}).
	 * 
	 * @param token
	 *            JSON Web Token to be decoded and parsed
	 * @throws SignatureVerificationException
	 *             if the signature of the token cannot be verified
	 * @throws TokenExpiredException
	 *             if the token has expired
	 * @throws JWTDecodeException
	 *             if the token cannot be decoded
	 * @throws NoSuchAlgorithmException
	 *             if the specified algorithm for verification cannot be found
	 */
	public void decodeAndParseJwt(String token)
			throws SignatureVerificationException, TokenExpiredException, JWTDecodeException, NoSuchAlgorithmException {
		try {
			DecodedJWT jwt = JWT.decode(token);
			String alg = jwt.getAlgorithm();
			Algorithm algorithm;
			switch (alg) {
			case "RS256":
				algorithm = Algorithm.RSA256(publicKey, null);
				break;
			case "RS384":
				algorithm = Algorithm.RSA384(publicKey, null);
				break;
			case "RS512":
				algorithm = Algorithm.RSA512(publicKey, null);
				break;
			default:
				throw new NoSuchAlgorithmException("Algorithm " + alg + " cannot be found");
			}
			JWTVerifier verifier = JWT.require(algorithm).acceptLeeway(1).build();
			verifier.verify(token);
			String decodedToken = jwt.getClaim("rgs").asString();
			Date expiresAt = jwt.getExpiresAt();
			long expirationTime = 0;
			if (expiresAt != null) {
				expirationTime = expiresAt.getTime();
			}
			credentials = new StringCredentials();
			credentials.parseAuthorization(decodedToken, expirationTime);
			this.decodedToken = decodedToken;
			this.expirationTime = expirationTime;
			this.token = token;
		} catch (com.auth0.jwt.exceptions.SignatureVerificationException e) {
			throw new SignatureVerificationException(e.getMessage(), e);
		} catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
			throw new TokenExpiredException(e.getMessage(), e);
		} catch (com.auth0.jwt.exceptions.JWTDecodeException e) {
			throw new JWTDecodeException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the entries of this credentials object as map.
	 * 
	 * @return map with access rights associated to resources.
	 */
	public Map<Resource, String> getEntries() {
		return credentials.getEntries();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Credentials#canCreate(org.arx.Resource)
	 */
	@Override
	public boolean canCreate(Resource resourcePattern) {
		return credentials.canCreate(resourcePattern);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Credentials#canUpdate(org.arx.Resource)
	 */
	@Override
	public boolean canUpdate(Resource resourcePattern) {
		return credentials.canUpdate(resourcePattern);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Credentials#canDelete(org.arx.Resource)
	 */
	@Override
	public boolean canDelete(Resource resourcePattern) {
		return credentials.canDelete(resourcePattern);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.arx.Credentials#canRead(org.arx.Resource)
	 */
	@Override
	public boolean canRead(Resource resourcePattern) {
		return credentials.canRead(resourcePattern);
	}


	/* (non-Javadoc)
	 * @see org.arx.Credentials#serialize()
	 */
	@Override
	public String serialize() {
		return String.format(JWT_FORMAT, token);
	}
	static {
		try {
			String publicKeyB64 = Configuration.getInstance().getParameter("org.arx.util.jwt.JwtCredentials.publicKey");
			if (publicKeyB64 == null) {
				LOGGER.error("No public key specified");
			} else {
				byte[] decoded = Base64.getDecoder().decode(publicKeyB64.trim());
				X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				publicKey = (RSAPublicKey) kf.generatePublic(spec);
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			LOGGER.error("Public key not valid", e);
		}

	}
}
