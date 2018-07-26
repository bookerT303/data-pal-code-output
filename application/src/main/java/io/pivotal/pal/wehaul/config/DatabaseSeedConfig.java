package io.pivotal.pal.wehaul.config;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruckRepository;
import io.pivotal.pal.wehaul.rental.domain.RentalTruck;
import io.pivotal.pal.wehaul.rental.domain.RentalTruckRepository;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class DatabaseSeedConfig {

    private final FleetTruckRepository fleetTruckRepository;
    private final RentalTruckRepository rentalTruckRepository;
    private final FleetTruck.Factory fleetTruckFactory;
    private final RentalTruck.Factory rentalTruckFactory;

    public DatabaseSeedConfig(FleetTruckRepository fleetTruckRepository,
                              RentalTruckRepository rentalTruckRepository,
                              FleetTruck.Factory fleetTruckFactory,
                              RentalTruck.Factory rentalTruckFactory) {
        this.fleetTruckRepository = fleetTruckRepository;
        this.rentalTruckRepository = rentalTruckRepository;
        this.fleetTruckFactory = fleetTruckFactory;
        this.rentalTruckFactory = rentalTruckFactory;
    }

    @PostConstruct
    public void populateDatabase() {
        // Create one Truck in both Fleet + Rental perspectives that is unrentable/in inspection
        String vin = "test-0001";
        FleetTruck inInspectionFleetTruck = fleetTruckFactory.buyTruck(vin, 0);
        fleetTruckRepository.save(inInspectionFleetTruck);

        RentalTruck unrentableRentalTruck = rentalTruckFactory
            .createRentableTruck(
                vin,
                inInspectionFleetTruck.getMakeModel().getMake(),
                inInspectionFleetTruck.getMakeModel().getModel()
            );
        unrentableRentalTruck.preventRenting();
        rentalTruckRepository.save(unrentableRentalTruck);


        // Create another Truck in both Fleet + Rental perspectives that is rentable/not in inspection
        String vin2 = "test-0002";
        FleetTruck inspectableFleetTruck = fleetTruckFactory.buyTruck(vin2, 0);
        inspectableFleetTruck.returnFromInspection("some notes", 0);
        fleetTruckRepository.save(inspectableFleetTruck);

        RentalTruck rentableRentalTruck = rentalTruckFactory
            .createRentableTruck(
                vin2,
                inspectableFleetTruck.getMakeModel().getMake(),
                inspectableFleetTruck.getMakeModel().getModel()
            );
        rentalTruckRepository.save(rentableRentalTruck);
    }
}
