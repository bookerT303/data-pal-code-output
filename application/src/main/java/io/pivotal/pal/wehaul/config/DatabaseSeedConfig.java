package io.pivotal.pal.wehaul.config;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruckRepository;
import io.pivotal.pal.wehaul.fleet.domain.TruckInspection;
import io.pivotal.pal.wehaul.fleet.domain.TruckInspectionRepository;
import io.pivotal.pal.wehaul.rental.domain.RentalTruck;
import io.pivotal.pal.wehaul.rental.domain.RentalTruckRepository;
import io.pivotal.pal.wehaul.rental.domain.TruckSizeLookupClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Configuration
public class DatabaseSeedConfig {

    private final FleetTruckRepository fleetTruckRepository;
    private final RentalTruckRepository rentalTruckRepository;
    private final TruckInspectionRepository truckInspectionRepository;
    private final FleetTruck.Factory fleetTruckFactory;
    private final TruckSizeLookupClient truckSizeLookupClient;

    public DatabaseSeedConfig(FleetTruckRepository fleetTruckRepository,
                              RentalTruckRepository rentalTruckRepository,
                              TruckInspectionRepository truckInspectionRepository,
                              FleetTruck.Factory fleetTruckFactory,
                              TruckSizeLookupClient truckSizeLookupClient) {
        this.fleetTruckRepository = fleetTruckRepository;
        this.rentalTruckRepository = rentalTruckRepository;
        this.truckInspectionRepository = truckInspectionRepository;
        this.fleetTruckFactory = fleetTruckFactory;
        this.truckSizeLookupClient = truckSizeLookupClient;
    }

    @PostConstruct
    public void populateDatabase() {
        // Create one Truck in both Fleet + Rental perspectives that is unrentable/in inspection
        String vin = "test-0001";
        FleetTruck inInspectionFleetTruck = fleetTruckFactory.buyTruck(vin, 0);
        fleetTruckRepository.save(inInspectionFleetTruck);

        RentalTruck unrentableRentalTruck = new RentalTruck.Factory(truckSizeLookupClient)
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
        inspectableFleetTruck.returnFromInspection(0);
        fleetTruckRepository.save(inspectableFleetTruck);

        truckInspectionRepository.save(TruckInspection.createTruckInspection(vin2, 0, "some notes"));

        RentalTruck rentableRentalTruck = new RentalTruck.Factory(truckSizeLookupClient)
                .createRentableTruck(
                        vin2,
                        inspectableFleetTruck.getMakeModel().getMake(),
                        inspectableFleetTruck.getMakeModel().getModel()
                );
        rentalTruckRepository.save(rentableRentalTruck);
    }
}
