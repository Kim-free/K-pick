package com.example.kpick.admob.service;

import com.example.kpick.admob.dto.req.AdMobSsvCallbackRequest;
import org.springframework.stereotype.Component;

@Component
public class AdMobSsvVerifier {
    public boolean validate(AdMobSsvCallbackRequest request) {
        // TODO: Verify signature/key_id using Google AdMob SSV public keys.
        // Keep this class isolated so Google public key fetching and ECDSA verification
        // can be added without changing callback reward logic.
        return true;
    }
}
