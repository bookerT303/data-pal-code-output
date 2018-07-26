package io.pivotal.pal.wehaul.domain;

import org.springframework.data.repository.CrudRepository;

public interface TruckRepository extends CrudRepository<Truck, String> {

    Truck findTop1ByStatus(TruckStatus status);
}
