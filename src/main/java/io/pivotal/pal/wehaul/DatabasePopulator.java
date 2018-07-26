package io.pivotal.pal.wehaul;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import io.pivotal.pal.wehaul.repository.TruckRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabasePopulator {

    private final TruckRepository truckRepository;

    public DatabasePopulator(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
    }

    @PostConstruct
    public void populateDatabase() {
        Truck truck = new Truck("test-0001", 0);
        truckRepository.save(truck);

        Truck truck2 = new Truck("test-0002", 0);
        truck2.setStatus(TruckStatus.RENTABLE);
        truckRepository.save(truck2);
    }
}
