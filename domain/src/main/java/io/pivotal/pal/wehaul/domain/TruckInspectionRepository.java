package io.pivotal.pal.wehaul.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TruckInspectionRepository extends CrudRepository<TruckInspection, UUID> {
}
