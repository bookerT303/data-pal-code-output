package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruckRepository;
import io.pivotal.pal.wehaul.fleet.domain.MakeModel;
import io.pivotal.pal.wehaul.fleet.domain.TruckInfoLookupClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FleetService {

    private final TruckInfoLookupClient truckInfoLookupClient;
    private final FleetTruckRepository fleetTruckRepository;

    public FleetService(TruckInfoLookupClient truckInfoLookupClient,
                        FleetTruckRepository fleetTruckRepository) {
        this.truckInfoLookupClient = truckInfoLookupClient;
        this.fleetTruckRepository = fleetTruckRepository;
    }

    public void buyTruck(String vin, int odometerReading) {
        MakeModel makeModel = truckInfoLookupClient.getMakeModelByVin(vin);

        FleetTruck truck = new FleetTruck(vin, odometerReading, makeModel);

        fleetTruckRepository.save(truck);
    }

    public void returnTruckFromInspection(String vin, String notes, int odometerReading) {
        FleetTruck truck = fleetTruckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.returnFromInspection(notes, odometerReading);
        fleetTruckRepository.save(truck);
    }

    public void sendTruckForInspection(String vin) {
        FleetTruck truck = fleetTruckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.sendForInspection();

        fleetTruckRepository.save(truck);
    }

    public void removeTruckFromYard(String vin) {
        FleetTruck truck = fleetTruckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.removeFromYard();

        fleetTruckRepository.save(truck);
    }

    public void returnTruckToYard(String vin, int distanceTraveled) {
        FleetTruck truck = fleetTruckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.returnToYard(distanceTraveled);

        fleetTruckRepository.save(truck);
    }

    public Collection<FleetTruck> findAll() {
        return StreamSupport
                .stream(fleetTruckRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public FleetTruck findOne(String vin) {
        return fleetTruckRepository.findOne(vin);
    }
}
