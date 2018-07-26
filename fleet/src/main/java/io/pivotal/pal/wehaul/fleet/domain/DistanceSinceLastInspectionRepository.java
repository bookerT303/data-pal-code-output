package io.pivotal.pal.wehaul.fleet.domain;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.Collection;

@NoRepositoryBean
public interface DistanceSinceLastInspectionRepository extends Repository<DistanceSinceLastInspection, String> {
    Collection<DistanceSinceLastInspection> findAll();
}
