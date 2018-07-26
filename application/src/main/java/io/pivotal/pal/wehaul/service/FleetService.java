package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.fleet.domain.DistanceSinceLastInspection;
import io.pivotal.pal.wehaul.fleet.domain.DistanceSinceLastInspectionRepository;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruckRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FleetService {

    private final FleetTruckRepository fleetTruckRepository;
    private final DistanceSinceLastInspectionRepository distanceSinceLastInspectionRepository;
    private final FleetTruck.Factory fleetTruckFactory;

    public FleetService(FleetTruckRepository fleetTruckRepository,
                        DistanceSinceLastInspectionRepository distanceSinceLastInspectionRepository,
                        FleetTruck.Factory fleetTruckFactory) {
        this.fleetTruckRepository = fleetTruckRepository;
        this.distanceSinceLastInspectionRepository = distanceSinceLastInspectionRepository;
        this.fleetTruckFactory = fleetTruckFactory;
    }

    public void buyTruck(String vin, int odometerReading) {
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, odometerReading);

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

    public Collection<DistanceSinceLastInspection> findAllDistanceSinceLastInspections() {
        return distanceSinceLastInspectionRepository.findAll();
    }

    public Collection<FleetTruck> findAll() {
        return StreamSupport
            .stream(fleetTruckRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
    }
}
