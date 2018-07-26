package io.pivotal.pal.wehaul.config;

import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruckCommandRepository;
import io.pivotal.pal.wehaul.fleet.domain.command.MakeModel;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckQueryRepository;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckSnapshot;
import io.pivotal.pal.wehaul.rental.domain.RentalTruck;
import io.pivotal.pal.wehaul.rental.domain.RentalTruckRepository;
import io.pivotal.pal.wehaul.rental.domain.RentalTruckSize;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class DatabaseSeedConfig {

    private final FleetTruckCommandRepository fleetTruckCommandRepository;
    private final RentalTruckRepository rentalTruckRepository;
    private final FleetTruckQueryRepository fleetTruckQueryRepository;

    public DatabaseSeedConfig(FleetTruckCommandRepository fleetTruckCommandRepository,
                              RentalTruckRepository rentalTruckRepository,
                              FleetTruckQueryRepository fleetTruckQueryRepository) {
        this.fleetTruckCommandRepository = fleetTruckCommandRepository;
        this.rentalTruckRepository = rentalTruckRepository;
        this.fleetTruckQueryRepository = fleetTruckQueryRepository;
    }

    @PostConstruct
    public void populateDatabase() {
        // Create one Truck in both Fleet + Rental perspectives that is unrentable/in inspection
        String vin = "test-0001";
        FleetTruck inInspectionFleetTruck =
                new FleetTruck(vin, 0, new MakeModel("TruckCo", "The Big One"));
        fleetTruckCommandRepository.save(inInspectionFleetTruck);
        fleetTruckQueryRepository.save(new FleetTruckSnapshot(inInspectionFleetTruck.getVin(), inInspectionFleetTruck.getStatus().toString(),
                inInspectionFleetTruck.getOdometerReading(), inInspectionFleetTruck.getMakeModel().getMake(), inInspectionFleetTruck.getMakeModel().getModel()));

        RentalTruck unrentableRentalTruck = new RentalTruck(vin, RentalTruckSize.LARGE);
        unrentableRentalTruck.preventRenting();
        rentalTruckRepository.save(unrentableRentalTruck);


        // Create another Truck in both Fleet + Rental perspectives that is rentable/not in inspection
        String vin2 = "test-0002";
        FleetTruck inspectableFleetTruck =
                new FleetTruck(vin2, 0, new MakeModel("TruckCo", "The Small One"));
        inspectableFleetTruck.returnFromInspection("some notes", 0);
        fleetTruckCommandRepository.save(inspectableFleetTruck);
        fleetTruckQueryRepository.save(new FleetTruckSnapshot(inspectableFleetTruck.getVin(), inspectableFleetTruck.getStatus().toString(),
                inspectableFleetTruck.getOdometerReading(), inspectableFleetTruck.getMakeModel().getMake(), inspectableFleetTruck.getMakeModel().getModel()));

        RentalTruck rentableRentalTruck = new RentalTruck(vin2, RentalTruckSize.SMALL);
        rentalTruckRepository.save(rentableRentalTruck);

    }
}
