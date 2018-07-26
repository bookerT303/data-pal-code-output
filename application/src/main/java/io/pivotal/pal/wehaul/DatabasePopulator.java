package io.pivotal.pal.wehaul;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruckRepository;
import io.pivotal.pal.wehaul.rental.domain.RentalTruck;
import io.pivotal.pal.wehaul.rental.domain.RentalTruckRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabasePopulator {

    private final FleetTruckRepository fleetTruckRepository;
    private final RentalTruckRepository rentalTruckRepository;

    public DatabasePopulator(FleetTruckRepository fleetTruckRepository, RentalTruckRepository rentalTruckRepository) {
        this.fleetTruckRepository = fleetTruckRepository;
        this.rentalTruckRepository = rentalTruckRepository;
    }

    @PostConstruct
    public void populateDatabase() {
        // Create one Truck in both Fleet + Rental perspectives that is unrentable/in inspection
        String vin = "test-0001";
        FleetTruck inInspectionFleetTruck = FleetTruck.buyTruck(vin, 0);
        fleetTruckRepository.save(inInspectionFleetTruck);

        RentalTruck unrentableRentalTruck = RentalTruck.createRentableTruck(vin);
        unrentableRentalTruck.preventRenting();
        rentalTruckRepository.save(unrentableRentalTruck);


        // Create another Truck in both Fleet + Rental perspectives that is rentable/not in inspection
        String vin2 = "test-0002";
        FleetTruck inspectableFleetTruck = FleetTruck.buyTruck(vin2, 0);
        inspectableFleetTruck.returnFromInspection(0);
        fleetTruckRepository.save(inspectableFleetTruck);

        RentalTruck rentableRentalTruck = RentalTruck.createRentableTruck(vin2);
        rentalTruckRepository.save(rentableRentalTruck);
    }
}