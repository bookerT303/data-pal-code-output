package io.pivotal.pal.wehaul.fleet.domain.command;

import io.pivotal.pal.wehaul.fleet.domain.command.event.*;
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
        events.forEach(event -> this.handleEvent(event));
        this.clearDomainEvents();
    }

    private void handleEvent(FleetTruckEvent event) {
        if (event instanceof FleetTruckPurchased) {
            handlePurchased((FleetTruckPurchased) event);
        } else if (event instanceof FleetTruckRemovedFromYard) {
            handleRemovedFromYard((FleetTruckRemovedFromYard) event);
        } else if (event instanceof FleetTruckReturnedFromInspection) {
            handleReturnedFromInspection((FleetTruckReturnedFromInspection) event);
        } else if (event instanceof FleetTruckReturnedToYard) {
            handleReturnedToYard((FleetTruckReturnedToYard) event);
        } else if (event instanceof FleetTruckSentForInspection) {
            handleSentForInspection((FleetTruckSentForInspection) event);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void purchaseTruck(String vin, int odometerReading, MakeModel makeModel) {
        if (odometerReading < 0) {
            throw new IllegalArgumentException("Cannot buy a truck with negative odometer reading");
        }

        FleetTruckPurchased event =
                new FleetTruckPurchased(vin, makeModel.getMake(), makeModel.getModel(), odometerReading);

        handlePurchased(event);
    }

    private void handlePurchased(FleetTruckPurchased event) {
        this.vin = event.getVin();
        this.status = FleetTruckStatus.IN_INSPECTION;
        this.odometerReading = event.getOdometerReading();
        this.makeModel = new MakeModel(event.getMake(), event.getModel());

        this.registerEvent(event);
    }

    public void returnFromInspection(String notes, int odometerReading) {
        if (status != FleetTruckStatus.IN_INSPECTION) {
            throw new IllegalStateException("Truck is not currently in inspection");
        }
        if (this.odometerReading > odometerReading) {
            throw new IllegalArgumentException("Odometer reading cannot be less than previous reading");
        }

        FleetTruckReturnedFromInspection event = new FleetTruckReturnedFromInspection(
                this.getVin(),
                odometerReading,
                notes
        );

        this.handleReturnedFromInspection(event);
    }

    private void handleReturnedFromInspection(FleetTruckReturnedFromInspection event) {
        this.status = FleetTruckStatus.INSPECTABLE;
        this.odometerReading = event.getOdometerReading();

        TruckInspection inspection =
                TruckInspection.createTruckInspection(vin, odometerReading, event.getNotes());
        this.inspections.add(inspection);

        this.registerEvent(event);
    }

    public void sendForInspection() {
        if (status != FleetTruckStatus.INSPECTABLE) {
            throw new IllegalStateException("Truck cannot be inspected");
        }

        FleetTruckSentForInspection event = new FleetTruckSentForInspection(this.getVin());
        this.handleSentForInspection(event);
    }

    private void handleSentForInspection(FleetTruckSentForInspection event) {
        this.status = FleetTruckStatus.IN_INSPECTION;
        this.registerEvent(event);
    }

    public void removeFromYard() {
        if (status != FleetTruckStatus.INSPECTABLE) {
            throw new IllegalStateException("Cannot prevent truck inspection");
        }

        FleetTruckRemovedFromYard event = new FleetTruckRemovedFromYard(this.getVin());
        this.handleRemovedFromYard(event);
    }

    private void handleRemovedFromYard(FleetTruckRemovedFromYard event) {
        this.status = FleetTruckStatus.NOT_INSPECTABLE;
        this.registerEvent(event);
    }

    public void returnToYard(int distanceTraveled) {
        if (status != FleetTruckStatus.NOT_INSPECTABLE) {
            throw new IllegalStateException("Cannot allow truck inspection");
        }

        FleetTruckReturnedToYard event = new FleetTruckReturnedToYard(this.getVin(), distanceTraveled);
        this.handleReturnedToYard(event);
    }

    private void handleReturnedToYard(FleetTruckReturnedToYard event) {
        this.status = FleetTruckStatus.INSPECTABLE;
        this.odometerReading += event.getDistanceTraveled();

        this.registerEvent(event);
    }

    public List<FleetTruckEvent> fleetDomainEvents() {
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
