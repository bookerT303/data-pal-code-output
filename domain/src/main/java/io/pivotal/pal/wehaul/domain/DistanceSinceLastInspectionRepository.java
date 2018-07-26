package io.pivotal.pal.wehaul.domain;

import java.util.Collection;

public interface DistanceSinceLastInspectionRepository {
    Collection<DistanceSinceLastInspection> findAllDistanceSinceLastInspections();
}
