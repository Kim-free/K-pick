package com.example.kpick.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AppleClientSecretProvider {
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final ResourceLoader resourceLoader;

    @Value("${oauth2.apple.client-id}")
    private String clientId;

    @Value("${oauth2.apple.team-id}")
    private String teamId;

    @Value("${oauth2.apple.key-id}")
    private String keyId;

    @Value("${oauth2.apple.private-key-path}")
    private String privateKeyPath;

    public AppleClientSecretProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String createClientSecret() {
        long issuedAt = Instant.now().getEpochSecond();
        long expiration = issuedAt + 300;
        String header = "{\"alg\":\"ES256\",\"kid\":\"" + keyId + "\"}";
        String payload = "{"
                + "\"iss\":\"" + teamId + "\","
                + "\"iat\":" + issuedAt + ","
                + "\"exp\":" + expiration + ","
                + "\"aud\":\"" + APPLE_ISSUER + "\","
                + "\"sub\":\"" + clientId + "\""
                + "}";
        String unsignedToken = base64Url(header) + "." + base64Url(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    private String sign(String unsignedToken) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(loadPrivateKey());
            signature.update(unsignedToken.getBytes(StandardCharsets.UTF_8));
            return BASE64_URL_ENCODER.encodeToString(convertDerSignatureToJose(signature.sign()));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create Apple client secret.", exception);
        }
    }

    private ECPrivateKey loadPrivateKey() throws Exception {
        Resource resource = resourceLoader.getResource(privateKeyPath);
        String pem;
        try (InputStream inputStream = resource.getInputStream()) {
            pem = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
        }
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private byte[] convertDerSignatureToJose(byte[] derSignature) {
        int offset = 2;
        if ((derSignature[1] & 0xff) >= 0x80) {
            offset += derSignature[1] & 0x7f;
        }
        if (derSignature[offset] != 0x02) {
            throw new IllegalArgumentException("Invalid Apple client secret signature.");
        }
        int rLength = derSignature[offset + 1] & 0xff;
        byte[] r = Arrays.copyOfRange(derSignature, offset + 2, offset + 2 + rLength);
        offset += 2 + rLength;
        if (derSignature[offset] != 0x02) {
            throw new IllegalArgumentException("Invalid Apple client secret signature.");
        }
        int sLength = derSignature[offset + 1] & 0xff;
        byte[] s = Arrays.copyOfRange(derSignature, offset + 2, offset + 2 + sLength);

        byte[] joseSignature = new byte[64];
        copyUnsigned(r, joseSignature, 0);
        copyUnsigned(s, joseSignature, 32);
        return joseSignature;
    }

    private void copyUnsigned(byte[] value, byte[] target, int targetOffset) {
        byte[] unsigned = new BigInteger(1, value).toByteArray();
        int sourceOffset = unsigned.length > 32 ? unsigned.length - 32 : 0;
        int length = Math.min(unsigned.length, 32);
        System.arraycopy(unsigned, sourceOffset, target, targetOffset + 32 - length, length);
    }

    private String base64Url(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
