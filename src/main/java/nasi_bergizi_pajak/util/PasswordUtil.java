package nasi_bergizi_pajak.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            return ALGORITHM + ":" + ITERATIONS + ":"
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Gagal melakukan hash password", e);
        }
    }

    public static boolean verifyPassword(String password, String storedPasswordHash) {
        try {
            String[] parts = storedPasswordHash.split(":");
            if (parts.length != 4) {
                return false;
            }

            String algorithm = parts[0];
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            if (!ALGORITHM.equals(algorithm)) {
                return false;
            }

            byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);
            return Arrays.equals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }
}
