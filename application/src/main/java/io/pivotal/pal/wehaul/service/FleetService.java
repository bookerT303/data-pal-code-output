package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class FleetService {

    private final Truck.Factory truckFactory;
    private final TruckRepository truckRepository;
    private final TruckInspectionRepository truckInspectionRepository;
    private final DistanceSinceLastInspectionRepository distanceSinceLastInspectionRepository;

    public FleetService(Truck.Factory truckFactory,
                        TruckRepository truckRepository,
                        TruckInspectionRepository truckInspectionRepository,
                        DistanceSinceLastInspectionRepository distanceSinceLastInspectionRepository) {
        this.truckFactory = truckFactory;
        this.truckRepository = truckRepository;
        this.truckInspectionRepository = truckInspectionRepository;
        this.distanceSinceLastInspectionRepository = distanceSinceLastInspectionRepository;
    }

    public void buyTruck(String vin, int odometerReading) {
        Truck truck = truckFactory.buyTruck(vin, odometerReading);

        truckRepository.save(truck);
    }

    @Transactional
    public void sendForInspection(String vin) {
        Truck truck = truckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.sendForInspection();

        truckRepository.save(truck);
    }

    @Transactional
    public void returnFromInspection(String vin, String notes, int odometerReading) {
        Truck truck = truckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        truck.returnFromInspection(odometerReading);
        truckRepository.save(truck);

        TruckInspection truckInspection = TruckInspection.createTruckInspection(vin, odometerReading, notes);
        truckInspectionRepository.save(truckInspection);
    }

    public Collection<DistanceSinceLastInspection> findAllDistanceSinceLastInspections() {
        return distanceSinceLastInspectionRepository.findAllDistanceSinceLastInspections();
    }

    public Collection<Truck> findAll() {
        Collection<Truck> trucks = new ArrayList<>();
        truckRepository.findAll().forEach(trucks::add);
        return trucks;
    }
}
