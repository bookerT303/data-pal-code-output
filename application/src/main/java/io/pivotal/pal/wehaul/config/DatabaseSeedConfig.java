package io.pivotal.pal.wehaul.config;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabaseSeedConfig {

    private final TruckRepository truckRepository;
    private final Truck.Factory truckFactory;

    public DatabaseSeedConfig(TruckRepository truckRepository, Truck.Factory truckFactory) {
        this.truckRepository = truckRepository;
        this.truckFactory = truckFactory;
    }

    @PostConstruct
    public void populateDatabase() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);
        truckRepository.save(truck);

        Truck truck2 = truckFactory.buyTruck("test-0002", 0);
        truck2.returnFromInspection(0);
        truckRepository.save(truck2);
    }
}
