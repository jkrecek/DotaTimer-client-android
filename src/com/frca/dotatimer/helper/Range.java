package com.frca.dotatimer.helper;

public final class Range {
    private final int min;
    private final int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public final int min() {
        return min;
    }

    public final int max() {
        return max;
    }
}
