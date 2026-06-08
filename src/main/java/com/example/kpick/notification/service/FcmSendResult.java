package com.example.kpick.notification.service;

public class FcmSendResult {
    private final int successCount;
    private final int failureCount;

    public FcmSendResult(int successCount, int failureCount) {
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    public static FcmSendResult skipped() {
        return new FcmSendResult(0, 0);
    }

    public boolean hasSuccess() {
        return successCount > 0;
    }

    public boolean hasFailure() {
        return failureCount > 0;
    }
}
