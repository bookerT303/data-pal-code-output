package io.pivotal.pal.wehaul.rental.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RentalRepository extends CrudRepository<Rental, UUID> {
}
