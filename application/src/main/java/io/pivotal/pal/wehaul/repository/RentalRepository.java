package io.pivotal.pal.wehaul.repository;

import io.pivotal.pal.wehaul.domain.Rental;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RentalRepository extends CrudRepository<Rental, UUID> {

}
