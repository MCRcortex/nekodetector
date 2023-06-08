package me.cortex.jarscanner.detection.impl;

public class AbstractDetection {
    private final String name;

    public AbstractDetection(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
