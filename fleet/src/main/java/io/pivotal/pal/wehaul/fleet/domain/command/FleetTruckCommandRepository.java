package io.pivotal.pal.wehaul.fleet.domain.command;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface FleetTruckCommandRepository extends Repository<FleetTruck, String> {
    FleetTruck save(FleetTruck fleetTruck);

    FleetTruck findOne(String vin);
}
