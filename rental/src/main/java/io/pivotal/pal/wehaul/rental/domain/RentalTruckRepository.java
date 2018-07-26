package io.pivotal.pal.wehaul.rental.domain;

import org.springframework.data.repository.CrudRepository;

public interface RentalTruckRepository extends CrudRepository<RentalTruck, String> {

    RentalTruck findTop1ByStatus(RentalTruckStatus status);
}
