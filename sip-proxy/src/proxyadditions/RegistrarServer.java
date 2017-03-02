package proxyadditions;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class RegistrarServer {

	private static RegistrarServer instance = null;
	private final DatabaseServer dbServer;

	private RegistrarServer() {
		dbServer = DatabaseServer.get();
	}

	public static RegistrarServer get() {
		if (instance == null)
			instance = new RegistrarServer();
		return instance;
	}
	
	public boolean existsUser(String username) {
		return dbServer.existsUser(username);
	}

	public boolean registerUser(String username, String password) {
		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] salt = generateSalt();
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 64 * 8);
			SecretKey key = skf.generateSecret(spec);
			byte[] res = key.getEncoded();
			return dbServer.addUser(username, res, salt);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			return false;
		}
	}

	private byte[] generateSalt() {
		final Random r = new SecureRandom();
		byte[] salt = new byte[16];
		r.nextBytes(salt);
		return salt;
	}
	
	public byte[] getSalt(String username) {
		return dbServer.getSalt(username);
	}

	public boolean authenticateUser(String username, String password) {
		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] salt = getSalt(username);
			if (salt == null) return false;
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 64 * 8);
			SecretKey key = skf.generateSecret(spec);
			byte[] res = key.getEncoded();
			return dbServer.authenticateUser(username, res);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return false;
		}
	}
	
	public boolean setRate(String username, int rate) {
		return dbServer.setUserRate(username, rate);
	}

	public double getUserBalance(String username) {
		return dbServer.getUserBalance(username);
	}
}