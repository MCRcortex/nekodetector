package me.cortex.jarscanner.detection;

import me.cortex.jarscanner.detection.impl.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper of all known {@link Detection} implementations.
 */
public final class DetectionManager {
    private static final DetectionManager instance = new DetectionManager();
    private final List<Detection> detections = new ArrayList<>();

    static {
        instance.addDetection(new FractureiserStage0a());
        instance.addDetection(new FractureiserStage0b());
        instance.addDetection(new FractureiserStage0c());
        instance.addDetection(new Skyragea());
        instance.addDetection(new Skyrageb());
    }

    private DetectionManager() {
        // deny construction
    }

    /**
     * @return Singleton instance of the manager.
     */
    @Nonnull
    public static DetectionManager getInstance() {
        return instance;
    }

    /**
     * @param detection Detection implementation to register.
     */
    public void addDetection(@Nonnull Detection detection) {
        this.detections.add(detection);
    }

    /**
     * @return All known detection implementations.
     */
    @Nonnull
    public List<Detection> getDetections() {
        return Collections.unmodifiableList(this.detections);
    }
}
