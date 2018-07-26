package io.pivotal.pal.wehaul.fleet.domain;

import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckReturnedFromInspection;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckSentForInspection;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "fleet_truck")
public class FleetTruck extends AbstractAggregateRoot {

    @Id
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column
    private FleetTruckStatus status;

    @Column
    private Integer odometerReading;

    @Embedded
    private MakeModel makeModel;

    @OneToMany(fetch = FetchType.EAGER, cascade = ALL)
    @JoinColumn(name = "truckVin", referencedColumnName = "vin")
    private List<TruckInspection> inspections = new ArrayList<>();

    FleetTruck() {
        // default constructor
    }

    public void returnFromInspection(String notes, int odometerReading) {
        if (status != FleetTruckStatus.IN_INSPECTION) {
            throw new IllegalStateException("Truck is not currently in inspection");
        }
        if (this.odometerReading > odometerReading) {
            throw new IllegalArgumentException("Odometer reading cannot be less than previous reading");
        }

        this.status = FleetTruckStatus.INSPECTABLE;
        this.odometerReading = odometerReading;

        TruckInspection inspection =
                TruckInspection.createTruckInspection(vin, odometerReading, notes);
        this.inspections.add(inspection);

        this.registerEvent(new FleetTruckReturnedFromInspection(this));
    }

    public void sendForInspection() {
        if (status != FleetTruckStatus.INSPECTABLE) {
            throw new IllegalStateException("Truck cannot be inspected");
        }

        this.status = FleetTruckStatus.IN_INSPECTION;

        this.registerEvent(new FleetTruckSentForInspection(this));
    }

    public void removeFromYard() {
        if (status != FleetTruckStatus.INSPECTABLE) {
            throw new IllegalStateException("Cannot prevent truck inspection");
        }

        this.status = FleetTruckStatus.NOT_INSPECTABLE;
    }

    public void returnToYard(int distanceTraveled) {
        if (status != FleetTruckStatus.NOT_INSPECTABLE) {
            throw new IllegalStateException("Cannot allow truck inspection");
        }
        this.status = FleetTruckStatus.INSPECTABLE;
        this.odometerReading += distanceTraveled;
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

    public static class Factory {

        private final TruckInfoLookupClient truckInfoLookupClient;

        public Factory(TruckInfoLookupClient truckInfoLookupClient) {
            this.truckInfoLookupClient = truckInfoLookupClient;
        }

        public FleetTruck buyTruck(String vin, int odometerReading) {
            if (odometerReading < 0) {
                throw new IllegalArgumentException("Cannot buy a truck with negative odometer reading");
            }
            FleetTruck fleetTruck = new FleetTruck();
            fleetTruck.vin = vin;
            fleetTruck.status = FleetTruckStatus.IN_INSPECTION;
            fleetTruck.odometerReading = odometerReading;
            fleetTruck.makeModel = truckInfoLookupClient.getMakeModelByVin(vin);

            FleetTruckPurchased fleetTruckPurchased = new FleetTruckPurchased(fleetTruck);
            fleetTruck.registerEvent(fleetTruckPurchased);

            return fleetTruck;
        }

    }
}
