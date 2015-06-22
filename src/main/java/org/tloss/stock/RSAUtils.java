package org.tloss.stock;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Properties;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

public class RSAUtils {
	public static final String ALGORITHM = "RSA";

	/**
	 * Encrypt the plain text using public key.
	 * 
	 * @param text
	 *            : original plain text
	 * @param key
	 *            :The public key
	 * @return Encrypted text
	 * @throws java.lang.Exception
	 */
	public static byte[] encrypt(String text, PublicKey key) {
		byte[] cipherText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cipherText;
	}

	/**
	 * Decrypt text using private key.
	 * 
	 * @param text
	 *            :encrypted text
	 * @param key
	 *            :The private key
	 * @return plain text
	 * @throws java.lang.Exception
	 */
	public static String decrypt(byte[] text, PrivateKey key) {
		byte[] dectyptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(ALGORITHM);

			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, key);
			dectyptedText = cipher.doFinal(text);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(dectyptedText);
	}

	public static KeyPair getKeyPair() {
		KeyPair keyPair = null;
		try {
			InputStream is = RSAUtils.class
					.getResourceAsStream("/keystore.jks");

			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, "123456789".toCharArray());

			String alias = "tungt84@gmail.com";

			Key key = keystore.getKey(alias, "123456789".toCharArray());
			if (key instanceof PrivateKey) {
				// Get certificate of public key
				Certificate cert = keystore.getCertificate(alias);

				// Get public key
				PublicKey publicKey = cert.getPublicKey();

				// Return a key pair
				keyPair = new KeyPair(publicKey, (PrivateKey) key);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return keyPair;
	}

	public static String getPassword(String resource) {
		String rs = null;
		try {
			Properties properties = new Properties();
			properties.load(RSAUtils.class
					.getResourceAsStream(resource));
			rs = RSAUtils.decrypt(
					Base64.decodeBase64(properties.getProperty("password")),
					RSAUtils.getKeyPair().getPrivate());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	public static String getPin(String resource) {
		String rs = null;
		try {
			Properties properties = new Properties();
			properties.load(RSAUtils.class
					.getResourceAsStream(resource));
			rs = RSAUtils.decrypt(
					Base64.decodeBase64(properties.getProperty("pin")),
					RSAUtils.getKeyPair().getPrivate());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
}
