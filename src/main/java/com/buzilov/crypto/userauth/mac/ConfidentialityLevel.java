package com.buzilov.crypto.userauth.mac;

public class ConfidentialityLevel {

    private static final int UNRESTRICTED = 0;
    private static final int RESTRICTED = 1;
    private static final int SECRET = 2;
    private static final int TOP_SECRET = 3;

    private int levelValue;

    private ConfidentialityLevel() {
    }

    private ConfidentialityLevel(int levelValue) {
        this.levelValue = levelValue;
    }

    public static ConfidentialityLevel unrestricted() {
        return new ConfidentialityLevel(UNRESTRICTED);
    }

    public static ConfidentialityLevel restricted() {
        return new ConfidentialityLevel(RESTRICTED);
    }

    public static ConfidentialityLevel secret() {
        return new ConfidentialityLevel(SECRET);
    }

    public static ConfidentialityLevel topSecret() {
        return new ConfidentialityLevel(TOP_SECRET);
    }

    public void setLevelValue(int levelValue) {
        this.levelValue = levelValue;
    }

    public int getLevelValue() {
        return levelValue;
    }
}
