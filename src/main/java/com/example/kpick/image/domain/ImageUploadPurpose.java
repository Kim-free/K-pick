package com.example.kpick.image.domain;

public enum ImageUploadPurpose {
    THREAD("threads"),
    PROFILE("profiles"),
    PROGRAM("programs"),
    INQUIRY("inquiries");

    private final String directory;

    ImageUploadPurpose(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }
}
