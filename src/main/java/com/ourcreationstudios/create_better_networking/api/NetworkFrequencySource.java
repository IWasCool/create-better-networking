package com.ourcreationstudios.create_better_networking.api;

import java.util.UUID;

/** A source of a UUID-based Create logistics network, including addon items. */
@FunctionalInterface
public interface NetworkFrequencySource {
    /** Returns the logistics network UUID, or null when unconnected. */
    UUID getNetworkFrequency();
}
