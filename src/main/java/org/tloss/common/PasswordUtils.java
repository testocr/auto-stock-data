package org.tloss.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

public class PasswordUtils {
	private static PrivateKey privateKey;
	private static PublicKey publicKey;

	public static void loadKeyStore() {
		KeyStore keystore;
		try {
			keystore = KeyStore.getInstance("JKS", "SUN");
			keystore.load(new FileInputStream("tloss.jks"),
					"tl0$$@2011".toCharArray());
			Key key = keystore.getKey("tloss", "tl0$$@2011".toCharArray());
			if (key instanceof PrivateKey) {
				Certificate cert = keystore.getCertificate("tloss");
				publicKey = cert.getPublicKey();
				privateKey = (PrivateKey) key;
				// return new KeyPair(publicKey, (PrivateKey) key);
			}
		} catch (KeyStoreException e) {

			e.printStackTrace();
		} catch (NoSuchProviderException e) {

			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		} catch (CertificateException e) {

			e.printStackTrace();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {

			e.printStackTrace();
		}

	}

	public static PublicKey getPublicKey() {
		return publicKey;
	}

	public static PrivateKey getPrivateKey() {
		return privateKey;
	}

	public static String encryt(String data) {
		Cipher c;
		try {
			c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, getPublicKey());
			byte[] datas = c.doFinal(data.getBytes());
			Base64 base64 = new Base64();
			String encodedString = base64.encodeToString(datas);
			return encodedString;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String decryt(String data) {
		Cipher c;
		try {
			c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, getPrivateKey());
			Base64 base64 = new Base64();
			byte[] datas = c.doFinal(base64.decode(data.getBytes()));
			return new String(datas);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, FileNotFoundException, IOException {
		loadKeyStore();
		String e =encryt("");
		System.out.println(e);
		System.out.println(decryt(e));
		Properties properties =  new Properties();
		properties.setProperty("password", e);
		properties.store(new FileOutputStream("temp.properties"), "");
	}
}
