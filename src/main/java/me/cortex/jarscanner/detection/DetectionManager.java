package me.cortex.jarscanner.detection;

import me.cortex.jarscanner.detection.impl.FractureiserStage0a;
import me.cortex.jarscanner.detection.impl.FractureiserStage0b;
import me.cortex.jarscanner.detection.impl.FractureiserStage0c;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper of all known {@link Detection} implementations.
 */
public class DetectionManager {
	private static final DetectionManager instance = new DetectionManager();
	private final List<Detection> detectionImplementations = new ArrayList<>();

	private DetectionManager() {
		// deny construction
	}

	/**
	 * @param detectionImplementation
	 * 		Detection implementation to register.
	 */
	public void addDetection(@Nonnull Detection detectionImplementation) {
		detectionImplementations.add(detectionImplementation);
	}

	/**
	 * @return All known detection implementations.
	 */
	@Nonnull
	public List<Detection> getDetectionImplementations() {
		return detectionImplementations;
	}

	/**
	 * @return Singleton instance of the manager.
	 */
	@Nonnull
	public static DetectionManager getInstance() {
		return instance;
	}

	static {
		instance.addDetection(new FractureiserStage0a());
		instance.addDetection(new FractureiserStage0b());
		instance.addDetection(new FractureiserStage0c());
	}
}
