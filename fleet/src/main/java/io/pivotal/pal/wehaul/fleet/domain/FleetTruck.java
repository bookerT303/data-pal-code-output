package io.pivotal.pal.wehaul.fleet.domain;

import io.pivotal.pal.wehaul.fleet.domain.event.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FleetTruck extends AbstractAggregateRoot {

    private String vin;

    private FleetTruckStatus status;

    private Integer odometerReading;

    private MakeModel makeModel;

    private List<TruckInspection> inspections = new ArrayList<>();

    public FleetTruck(String vin, int odometerReading, MakeModel makeModel) {
        this.purchaseTruck(vin, odometerReading, makeModel);
    }

    FleetTruck() {
        // default constructor
    }

    public FleetTruck(List<FleetTruckEvent> events) {
        events.forEach(event -> this.replayEvent(event));
        clearDomainEvents();
    }

    private void replayEvent(FleetTruckEvent event) {
        if (event instanceof FleetTruckPurchased) {
            handleEvent((FleetTruckPurchased) event);
        } else if (event instanceof FleetTruckRemovedFromYard) {
            handleEvent((FleetTruckRemovedFromYard) event);
        } else if (event instanceof FleetTruckReturnedFromInspection) {
            handleEvent((FleetTruckReturnedFromInspection) event);
        } else if (event instanceof FleetTruckReturnedToYard) {
            handleEvent((FleetTruckReturnedToYard) event);
        } else if (event instanceof FleetTruckReturnedToYard) {
            handleEvent((FleetTruckReturnedToYard) event);
        } else if (event instanceof FleetTruckSentForInspection) {
            handleEvent((FleetTruckSentForInspection) event);
        } else {
            System.out.println("Not able to apply a "+event.getClass().getName());
        }
    }

    private void handleEvent(FleetTruckPurchased event) {
        this.vin = event.getVin();
        this.status = FleetTruckStatus.IN_INSPECTION;
        this.odometerReading = event.getOdometerReading();
        this.makeModel = new MakeModel(event.getMake(), event.getModel());

        this.registerEvent(event);
    }

    private void handleEvent(FleetTruckRemovedFromYard event) {
        this.status = FleetTruckStatus.NOT_INSPECTABLE;
        this.registerEvent(event);
    }

    private void handleEvent(FleetTruckReturnedFromInspection event) {

        this.status = FleetTruckStatus.INSPECTABLE;
        this.odometerReading = event.getOdometerReading();

        TruckInspection inspection =
                TruckInspection.createTruckInspection(event.getVin(), event.getOdometerReading(),
                        event.getNotes());
        this.inspections.add(inspection);

        this.registerEvent(event);
    }

    private void handleEvent(FleetTruckReturnedToYard event) {
        this.status = FleetTruckStatus.INSPECTABLE;
        this.odometerReading += event.getDistanceTraveled();

        this.registerEvent(event);
    }


    private void handleEvent(FleetTruckSentForInspection event) {
        this.status = FleetTruckStatus.IN_INSPECTION;

        this.registerEvent(event);
    }

    private void purchaseTruck(String vin, int odometerReading, MakeModel makeModel) {
        if (odometerReading < 0) {
            throw new IllegalArgumentException("Cannot buy a truck with negative odometer reading");
        }

        FleetTruckPurchased event =
                new FleetTruckPurchased(vin, makeModel.getMake(), makeModel.getModel(), odometerReading);

        handleEvent(event);
    }

    public void returnFromInspection(String notes, int odometerReading) {
        if (status != FleetTruckStatus.IN_INSPECTION) {
            throw new IllegalStateException("Truck is not currently in inspection");
        }
        if (this.odometerReading > odometerReading) {
            throw new IllegalArgumentException("Odometer reading cannot be less than previous reading");
        }

        handleEvent(new FleetTruckReturnedFromInspection(
                this.getVin(),
                odometerReading,
                notes
        ));
    }

    public void sendForInspection() {
        if (status != FleetTruckStatus.INSPECTABLE) {
            throw new IllegalStateException("Truck cannot be inspected");
        }

        handleEvent(new FleetTruckSentForInspection(vin));
    }

    public void removeFromYard() {
        if (status != FleetTruckStatus.INSPECTABLE) {
            throw new IllegalStateException("Cannot prevent truck inspection");
        }

        FleetTruckRemovedFromYard event = new FleetTruckRemovedFromYard(this.getVin());
        handleEvent(event);
    }

    public void returnToYard(int distanceTraveled) {
        if (status != FleetTruckStatus.NOT_INSPECTABLE) {
            throw new IllegalStateException("Cannot allow truck inspection");
        }

        FleetTruckReturnedToYard event = new FleetTruckReturnedToYard(this.getVin(), distanceTraveled);
        handleEvent(event);
    }

    public List<FleetTruckEvent> unsavedFleetDomainEvents() {
        return domainEvents().stream()
                .map(obj -> (FleetTruckEvent) obj)
                .collect(Collectors.toList());
    }

    public String getVin() {
        return vin;
    }

    public FleetTruckStatus getStatus() {
        return status;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    public MakeModel getMakeModel() {
        return makeModel;
    }

    public List<TruckInspection> getInspections() {
        return inspections;
    }

    @Override
    public String toString() {
        return "FleetTruck{" +
                "vin='" + vin + '\'' +
                ", status=" + status +
                ", odometerReading=" + odometerReading +
                ", makeModel=" + makeModel +
                ", inspections=" + inspections +
                '}';
    }
}
