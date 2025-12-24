package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Locations {
    private static final Log LOG = LogFactory.getLog(Locations.class);

        private final List<Location> locations = new ArrayList<>();

        public Locations(String... rawLocations) {
        List<Location> normalizedLocations = new ArrayList<>();
        for (String rawLocation : rawLocations) {
            normalizedLocations.add(new Location(rawLocation));
        }
        processLocations(normalizedLocations);
    }

        public Locations(List<Location> rawLocations) {
        processLocations(rawLocations);
    }

    private void processLocations(List<Location> rawLocations) {
        List<Location> sortedLocations = new ArrayList<>(rawLocations);
        Collections.sort(sortedLocations);

        for (Location normalizedLocation : sortedLocations) {
            if (locations.contains(normalizedLocation)) {
                LOG.warn("Discarding duplicate location '" + normalizedLocation + "'");
                continue;
            }

            Location parentLocation = getParentLocationIfExists(normalizedLocation, locations);
            if (parentLocation != null) {
                LOG.warn("Discarding location '" + normalizedLocation + "' as it is a sublocation of '" + parentLocation + "'");
                continue;
            }

            locations.add(normalizedLocation);
        }
    }

        public List<Location> getLocations() {
        return locations;
    }

        private Location getParentLocationIfExists(Location location, List<Location> finalLocations) {
        for (Location finalLocation : finalLocations) {
            if (finalLocation.isParentOf(location)) {
                return finalLocation;
            }
        }
        return null;
    }
}