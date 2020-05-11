package com.utils.util;

import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * RSA 工具类
 *
 * @author 谢长春 2020-05-11
 */
public final class Rsa {
    private Rsa() {
    }

    private static final String RSA_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * 创建公钥私钥
     *
     * @return {@link KeyStore}
     */
    @SneakyThrows
    public static KeyStore createKeys() {
        final KeyPairGenerator keyPairGeno = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGeno.initialize(1024);
        final KeyPair keyPair = keyPairGeno.generateKeyPair();

        final RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        final RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        final KeyStore keyStore = new KeyStore();
        keyStore.setPublicKey(Base64.encodeBase64String(publicKey.getEncoded()));
        keyStore.setPrivateKey(Base64.encodeBase64String(privateKey.getEncoded()));
        return keyStore;
    }

    /**
     * 获取公钥对象，初始化公钥实例
     *
     * @param pubKey {@link String} 公钥字符串
     * @return {@link RSAPublicKey}
     */
    @SneakyThrows
    public static RSAPublicKey getPublicKey(final String pubKey) {
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(pubKey));
        final KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * 通过私钥 byte[] 将公钥还原，适用于RSA算法
     *
     * @param priKey {@link String} 私钥字符串
     * @return {@link RSAPrivateKey}
     */
    @SneakyThrows
    public static RSAPrivateKey getPrivateKey(final String priKey) {
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(priKey));
        final KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * 公钥加密
     *
     * @param data      {@link String} 明文
     * @param publicKey {@link String} 公钥
     * @return {@link String} 密文
     */
    public static String encryptByPublicKey(final String data, final String publicKey) {
        return encryptByPublicKey(data, getPublicKey(publicKey));
    }

    /**
     * 公钥加密
     *
     * @param data      {@link String} 明文
     * @param publicKey {@link RSAPublicKey} 公钥
     * @return {@link String} 密文
     */
    @SneakyThrows
    public static String encryptByPublicKey(final String data, final RSAPublicKey publicKey) {
        final Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        final byte[] bytes = cipher.doFinal(data.getBytes(UTF_8));
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 公钥解密
     *
     * @param data         {@link String} 密文
     * @param rsaPublicKey {@link String} 公钥
     * @return {@link String} 明文
     */
    public static String decryptByPublicKey(final String data, final String rsaPublicKey) {
        return decryptByPublicKey(data, getPublicKey(rsaPublicKey));
    }

    /**
     * 公钥解密
     *
     * @param data         {@link String} 密文
     * @param rsaPublicKey {@link RSAPublicKey} 公钥
     * @return {@link String} 明文
     */
    @SneakyThrows
    public static String decryptByPublicKey(final String data, final RSAPublicKey rsaPublicKey) {
        final Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
        final byte[] inputData = Base64.decodeBase64(data);
        final byte[] bytes = cipher.doFinal(inputData);
        return new String(bytes, UTF_8);
    }

    /**
     * 私钥加密
     *
     * @param data       {@link String} 明文
     * @param privateKey {@link String} 私钥
     * @return {@link String} 密文
     */
    public static String encryptByPrivateKey(final String data, final String privateKey) {
        return encryptByPrivateKey(data, getPrivateKey(privateKey));
    }

    /**
     * 私钥加密
     *
     * @param data       {@link String} 明文
     * @param privateKey {@link RSAPrivateKey} 私钥
     * @return {@link String} 密文
     */
    @SneakyThrows
    public static String encryptByPrivateKey(final String data, final RSAPrivateKey privateKey) {
        final Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        final byte[] bytes = cipher.doFinal(data.getBytes(UTF_8));
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 私钥解密
     *
     * @param data       {@link String} 密文
     * @param privateKey {@link String} 私钥
     * @return {@link String} 明文
     */
    public static String decryptByPrivateKey(final String data, final String privateKey) {
        return decryptByPrivateKey(data, getPrivateKey(privateKey));
    }

    /**
     * 私钥解密
     *
     * @param data       {@link String} 密文
     * @param privateKey {@link RSAPrivateKey} 私钥
     * @return {@link String} 明文
     */
    @SneakyThrows
    public static String decryptByPrivateKey(final String data, final RSAPrivateKey privateKey) {
        final Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        final byte[] inputData = Base64.decodeBase64(data);
        final byte[] bytes = cipher.doFinal(inputData);
        return new String(bytes, UTF_8);
    }

    /**
     * 用私钥对数据生成数字签名
     *
     * @param data       {@link String} 被签名数据
     * @param privateKey {@link String} 私钥
     * @return {@link String} 数字签名串
     */
    @SneakyThrows
    public static String signature(final String data, final String privateKey) {
        return signature(data, getPrivateKey(privateKey));
    }

    /**
     * 用私钥对数据生成数字签名
     *
     * @param data       {@link String} 被签名数据
     * @param privateKey {@link String} 私钥
     * @return {@link String} 数字签名串
     */
    @SneakyThrows
    public static String signature(final String data, final RSAPrivateKey privateKey) {
        final Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data.getBytes(UTF_8));
        return Base64.encodeBase64String(signature.sign());
    }

    /**
     * 校验数字签名
     *
     * @param data      {@link String} 被签名数据
     * @param publicKey {@link String} 公钥
     * @param sign      {@link String} 数字签名串
     * @return {@link Boolean} true：校验成功， false：校验失败
     */
    @SneakyThrows
    public static boolean verify(final String data, final String publicKey, final String sign) {
        return verify(data, getPublicKey(publicKey), sign);
    }


    /**
     * 校验数字签名
     *
     * @param data      {@link String} 被签名数据
     * @param publicKey {@link String} 公钥
     * @param sign      {@link String} 数字签名串
     * @return {@link Boolean} true：校验成功， false：校验失败
     */
    @SneakyThrows
    public static boolean verify(final String data, final RSAPublicKey publicKey, final String sign) {
        final Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data.getBytes(UTF_8));
        return signature.verify(Base64.decodeBase64(sign));
    }

    @Data
    public static class KeyStore {
        /**
         * 公钥
         */
        private String publicKey;
        /**
         * 私钥
         */
        private String privateKey;
    }

    public static void main(String[] args) {
        final KeyStore keys = createKeys();
        final String privateKey = keys.getPrivateKey();
        final String publicKey = keys.getPublicKey();
        System.out.println("私钥：" + privateKey);
        System.out.println("公钥：" + publicKey);
        System.out.println("私钥：private.pem\n-----BEGIN RSA PRIVATE KEY-----\n"
                + privateKey.replaceAll("(.{64})", "$1\n")
                + "\n-----END RSA PRIVATE KEY-----");
        System.out.println("公钥：public.pem\n-----BEGIN RSA PUBLIC KEY-----\n"
                + publicKey.replaceAll("(.{64})", "$1\n")
                + "\n-----END RSA PUBLIC KEY-----");

        String privateContent = encryptByPrivateKey("zh_CN 中文", privateKey);
        System.out.println("私钥加密：" + privateContent);
        try {
            System.out.println("私钥加密 => 私钥解密：" + decryptByPrivateKey(privateContent, privateKey));
        } catch (Exception e) {
            System.out.println("私钥加密 => 私钥解密：失败");
        }
        try {
            System.out.println("私钥加密 => 公钥解密：" + decryptByPublicKey(privateContent, publicKey));
        } catch (Exception e) {
            System.out.println("私钥加密 => 公钥解密：失败");
        }

        String publicContent = encryptByPublicKey("zh_CN 中文", publicKey);
        System.out.println("公钥加密：" + publicContent);
        try {
            System.out.println("公钥加密 => 公钥解密：" + decryptByPublicKey(publicContent, publicKey));
        } catch (Exception e) {
            System.out.println("公钥加密 => 公钥解密：失败");
        }
        try {
            System.out.println("公钥加密 => 私钥解密：" + decryptByPrivateKey(publicContent, privateKey));
        } catch (Exception e) {
            System.out.println("公钥加密 => 私钥解密：失败");
        }

        final String signature = signature(privateContent, privateKey);
        System.out.println("私钥签名：" + signature);
        System.out.println("公钥验签：" + verify(privateContent, publicKey, signature));
    }
}
