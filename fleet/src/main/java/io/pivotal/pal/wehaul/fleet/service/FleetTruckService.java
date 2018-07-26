package io.pivotal.pal.wehaul.fleet.service;

import io.pivotal.pal.wehaul.fleet.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class FleetTruckService {

    private final FleetTruckRepository fleetTruckRepository;
    private final TruckInspectionRepository truckInspectionRepository;
    private final TruckSinceInspectionRepository truckSinceInspectionRepository;

    public FleetTruckService(FleetTruckRepository fleetTruckRepository,
                             TruckInspectionRepository truckInspectionRepository,
                             TruckSinceInspectionRepository truckSinceInspectionRepository) {
        this.fleetTruckRepository = fleetTruckRepository;
        this.truckInspectionRepository = truckInspectionRepository;
        this.truckSinceInspectionRepository = truckSinceInspectionRepository;
    }

    public void buyTruck(String vin, int odometerReading) {
    }

    @Transactional
    public void sendForInspection(String vin) {
        // TODO: implement me
        // HINT: use the tests and previous implementation as guides
    }

    @Transactional
    public void returnFromInspection(String vin, String notes, int odometerReading) {
        // TODO: implement me
        // HINT: use the tests and previous implementation as guides
    }

    public void removeFromYard(String vin) {
        // TODO: implement me
    }

    public void returnToYard(String vin, int distanceTraveled) {
        // TODO: implement me
    }

    public Collection<TruckSinceInspection> findAllTruckSinceInspections() {
        return truckSinceInspectionRepository.findAllTruckSinceInspections();
    }

    public Collection<FleetTruck> findAll() {
        Collection<FleetTruck> trucks = new ArrayList<>();
        fleetTruckRepository.findAll().forEach(trucks::add);
        return trucks;
    }
}
