package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.fleet.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FleetTruckService {

    private final FleetTruckRepository fleetTruckRepository;
    private final TruckInspectionRepository truckInspectionRepository;
    private final DistanceSinceLastInspectionRepository distanceSinceLastInspectionRepository;
    private final FleetTruck.Factory fleetTruckFactory;

    public FleetTruckService(FleetTruckRepository fleetTruckRepository,
                             TruckInspectionRepository truckInspectionRepository,
                             DistanceSinceLastInspectionRepository distanceSinceLastInspectionRepository,
                             FleetTruck.Factory fleetTruckFactory) {
        this.fleetTruckRepository = fleetTruckRepository;
        this.truckInspectionRepository = truckInspectionRepository;
        this.distanceSinceLastInspectionRepository = distanceSinceLastInspectionRepository;
        this.fleetTruckFactory = fleetTruckFactory;
    }

    public FleetTruck buyTruck(String vin, int odometerReading) {
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, odometerReading);

        fleetTruckRepository.save(truck);

        return truck;
    }

    @Transactional
    public void returnFromInspection(String vin, String notes, int odometerReading) {
        FleetTruck truck = fleetTruckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.returnFromInspection(odometerReading);
        fleetTruckRepository.save(truck);

        TruckInspection truckInspection =
                TruckInspection.createTruckInspection(vin, odometerReading, notes);
        truckInspectionRepository.save(truckInspection);
    }

    public void sendForInspection(String vin) {
        FleetTruck truck = fleetTruckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.sendForInspection();

        fleetTruckRepository.save(truck);
    }

    public void removeFromYard(String vin) {
        FleetTruck truck = fleetTruckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.removeFromYard();

        fleetTruckRepository.save(truck);
    }

    public void returnToYard(String vin, int distanceTraveled) {
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
