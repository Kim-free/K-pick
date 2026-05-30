package com.example.kpick.auth.service;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Component
public class AppleIdentityTokenVerifier {
    private final ApplePublicKeyClient applePublicKeyClient;

    public AppleIdentityTokenVerifier(ApplePublicKeyClient applePublicKeyClient) {
        this.applePublicKeyClient = applePublicKeyClient;
    }

    public void verify(String identityToken, String kid, String alg) {
        if (!"RS256".equals(alg)) {
            throw new IllegalArgumentException("Unsupported Apple identityToken alg.");
        }
        String[] tokenParts = identityToken.split("\\.");
        if (tokenParts.length != 3) {
            throw new IllegalArgumentException("Invalid Apple identityToken.");
        }

        ApplePublicKey applePublicKey = applePublicKeyClient.findByKidAndAlg(kid, alg);
        if (!verifySignature(tokenParts, applePublicKey)) {
            throw new IllegalArgumentException("Invalid Apple identityToken signature.");
        }
    }

    private boolean verifySignature(String[] tokenParts, ApplePublicKey applePublicKey) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(applePublicKey.getN());
            byte[] exponentBytes = Base64.getUrlDecoder().decode(applePublicKey.getE());
            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update((tokenParts[0] + "." + tokenParts[1]).getBytes());
            return signature.verify(Base64.getUrlDecoder().decode(tokenParts[2]));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to verify Apple identityToken signature.", exception);
        }
    }
}
