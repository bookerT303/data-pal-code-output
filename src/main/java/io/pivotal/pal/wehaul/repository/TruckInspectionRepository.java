package io.pivotal.pal.wehaul.repository;

import io.pivotal.pal.wehaul.domain.TruckInspection;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TruckInspectionRepository extends CrudRepository<TruckInspection, UUID> {
}
