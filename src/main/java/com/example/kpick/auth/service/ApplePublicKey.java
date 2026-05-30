package com.example.kpick.auth.service;

public class ApplePublicKey {
    private final String kid;
    private final String alg;
    private final String n;
    private final String e;

    public ApplePublicKey(String kid, String alg, String n, String e) {
        this.kid = kid;
        this.alg = alg;
        this.n = n;
        this.e = e;
    }

    public String getKid() {
        return kid;
    }

    public String getAlg() {
        return alg;
    }

    public String getN() {
        return n;
    }

    public String getE() {
        return e;
    }
}
