package io.pivotal.pal.wehaul.rental.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RentalTruckRepository extends CrudRepository<RentalTruck, String> {

    RentalTruck findTop1ByStatus(RentalTruckStatus status);

    RentalTruck findOneByRentalConfirmationNumber(UUID confirmationNumber);

}
