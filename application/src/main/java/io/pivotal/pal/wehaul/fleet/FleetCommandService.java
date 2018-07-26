package io.pivotal.pal.wehaul.fleet;

import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruckCommandRepository;
import io.pivotal.pal.wehaul.fleet.domain.command.MakeModel;
import io.pivotal.pal.wehaul.fleet.domain.command.TruckInfoLookupClient;
import org.springframework.stereotype.Service;

@Service
public class FleetCommandService {

    private final TruckInfoLookupClient truckInfoLookupClient;
    private final FleetTruckCommandRepository fleetTruckCommandRepository;

    public FleetCommandService(TruckInfoLookupClient truckInfoLookupClient,
                               FleetTruckCommandRepository fleetTruckCommandRepository) {
        this.truckInfoLookupClient = truckInfoLookupClient;
        this.fleetTruckCommandRepository = fleetTruckCommandRepository;
    }

    public void buyTruck(String vin, int odometerReading) {
        MakeModel makeModel = truckInfoLookupClient.getMakeModelByVin(vin);

        FleetTruck truck = new FleetTruck(vin, odometerReading, makeModel);

        fleetTruckCommandRepository.save(truck);
    }

    public void returnTruckFromInspection(String vin, String notes, int odometerReading) {
        FleetTruck truck = fleetTruckCommandRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.returnFromInspection(notes, odometerReading);
        fleetTruckCommandRepository.save(truck);
    }

    public void sendTruckForInspection(String vin) {
        FleetTruck truck = fleetTruckCommandRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.sendForInspection();

        fleetTruckCommandRepository.save(truck);
    }

    public void removeTruckFromYard(String vin) {
        FleetTruck truck = fleetTruckCommandRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.removeFromYard();

        fleetTruckCommandRepository.save(truck);
    }

    public void returnTruckToYard(String vin, int distanceTraveled) {
        FleetTruck truck = fleetTruckCommandRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.returnToYard(distanceTraveled);

        fleetTruckCommandRepository.save(truck);
    }
}
