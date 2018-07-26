package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckInspection;
import io.pivotal.pal.wehaul.domain.TruckSinceInspection;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import io.pivotal.pal.wehaul.repository.TruckInspectionRepository;
import io.pivotal.pal.wehaul.repository.TruckRepository;
import io.pivotal.pal.wehaul.repository.TruckSinceInspectionRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class FleetTruckService {

    private final TruckRepository truckRepository;
    private final TruckInspectionRepository truckInspectionRepository;
    private final TruckSinceInspectionRepository truckSinceInspectionRepository;

    public FleetTruckService(TruckRepository truckRepository,
                             TruckInspectionRepository truckInspectionRepository,
                             TruckSinceInspectionRepository truckSinceInspectionRepository) {
        this.truckRepository = truckRepository;
        this.truckInspectionRepository = truckInspectionRepository;
        this.truckSinceInspectionRepository = truckSinceInspectionRepository;
    }

    public void buyTruck(String vin,
                         int odometerReading) {
        if (odometerReading < 0) {
            throw new IllegalArgumentException("Cannot buy a truck with negative odometer reading");
        }
        Truck truck = new Truck(vin, odometerReading);
        truckRepository.save(truck);
    }

    @Transactional
    public void sendForInspection(String vin) {
        Truck truck = truckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }
        if (truck.getStatus() != TruckStatus.RENTABLE) {
            throw new IllegalStateException(
                String.format("Cannot send truck for inspection while truck is %s", truck.getStatus())
            );
        }

        truck.setStatus(TruckStatus.IN_INSPECTION);

        truckRepository.save(truck);
    }

    @Transactional
    public void returnFromInspection(String vin, String notes, int odometerReading) {
        Truck truck = truckRepository.findOne(vin);

        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with VIN=%s", vin));
        }

        if (truck.getStatus() != TruckStatus.IN_INSPECTION) {
            throw new IllegalStateException(
                String.format("Cannot return truck from inspection while truck is %s", truck.getStatus())
            );
        }
        if (truck.getOdometerReading() > odometerReading) {
            throw new IllegalArgumentException("Odometer reading cannot be less than previous reading");
        }

        truck.setStatus(TruckStatus.RENTABLE);
        truck.setOdometerReading(odometerReading);
        truckRepository.save(truck);

        TruckInspection truckInspection =
            new TruckInspection(vin, odometerReading, notes);
        truckInspectionRepository.save(truckInspection);
    }

    public Collection<TruckSinceInspection> findAllTruckSinceInspections() {
        return truckSinceInspectionRepository.findAllTruckSinceInspections();
    }

    public Collection<Truck> findAll() {
        Collection<Truck> trucks = new ArrayList<>();
        truckRepository.findAll().forEach(trucks::add);
        return trucks;
    }
}
