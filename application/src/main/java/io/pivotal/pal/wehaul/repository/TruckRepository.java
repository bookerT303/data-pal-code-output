package io.pivotal.pal.wehaul.repository;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import org.springframework.data.repository.CrudRepository;

public interface TruckRepository extends CrudRepository<Truck, String> {

    Truck findTop1ByStatus(TruckStatus status);
}
