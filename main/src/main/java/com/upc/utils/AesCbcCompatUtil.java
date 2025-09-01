package com.upc.utils;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public final class AesCbcCompatUtil {

    // 与 CryptoJS 的 Latin1.parse("qiaolian@haihang") 字节一致
    private static final byte[] KEY = "qiaolian@haihang".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] IV  = "qiaolian@haihang".getBytes(StandardCharsets.ISO_8859_1);
    private static final int BLOCK = 16;

    private AesCbcCompatUtil() {}

    /** AES/CBC/PKCS7 （JCE 名字是 PKCS5Padding） */
    public static String encryptPkcs7Base64(String plain) {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), new IvParameterSpec(IV));
            byte[] out = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("encryptPkcs7 failed", e);
        }
    }

    public static String decryptPkcs7Base64(String base64) {
        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), new IvParameterSpec(IV));
            byte[] out = c.doFinal(Base64.getDecoder().decode(base64));
            return new String(out, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("decryptPkcs7 failed", e);
        }
    }

    /** AES/CBC/ZeroPadding（JCE 用 NoPadding，自行补零/去零） */
    public static String encryptZeroBase64(String plain) {
        try {
            byte[] data = plain.getBytes(StandardCharsets.UTF_8);
            byte[] padded = zeroPad(data);
            Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), new IvParameterSpec(IV));
            return Base64.getEncoder().encodeToString(c.doFinal(padded));
        } catch (Exception e) {
            throw new RuntimeException("encryptZero failed", e);
        }
    }

    public static String decryptZeroBase64(String base64) {
        try {
            byte[] cipher = Base64.getDecoder().decode(base64);
            Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), new IvParameterSpec(IV));
            byte[] out = c.doFinal(cipher);
            return new String(trimTrailingZero(out), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("decryptZero failed", e);
        }
    }

    private static byte[] zeroPad(byte[] in) {
        int pad = (BLOCK - (in.length % BLOCK)) % BLOCK; // 0..15
        byte[] out = new byte[in.length + pad];
        System.arraycopy(in, 0, out, 0, in.length); // 其余天然为 0
        return out;
    }
    private static byte[] trimTrailingZero(byte[] in) {
        int i = in.length;
        while (i > 0 && in[i-1] == 0) i--;
        return Arrays.copyOf(in, i);
    }

    // —— 自检 ——：一键打印两种填充的密文与明文
    public static void main(String[] args) {
        String p = "Aa123456+";

        String ctPKCS7 = encryptPkcs7Base64(p);
        String ctZERO  = encryptZeroBase64(p);
        System.out.println("PKCS7: " + ctPKCS7);
        System.out.println("ZERO : " + ctZERO);
        System.out.println("PKCS7↘ " + decryptPkcs7Base64(ctPKCS7));
        System.out.println("ZERO ↘ " + decryptZeroBase64(ctZERO));
    }
}
