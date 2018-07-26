package io.pivotal.pal.wehaul.fleet.domain.query;

import org.springframework.data.repository.Repository;

import java.util.List;

public interface FleetTruckQueryRepository extends Repository<FleetTruckSnapshot, String> {
    List<FleetTruckSnapshot> findAll();

    FleetTruckSnapshot findOne(String vin);

    void save(FleetTruckSnapshot fleetTruckSnapshot);
}
