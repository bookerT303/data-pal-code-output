package io.pivotal.pal.wehaul.fleet.domain;

import java.util.Collection;

public interface DistanceSinceLastInspectionRepository {
    Collection<DistanceSinceLastInspection> findAllDistanceSinceLastInspections();
}
