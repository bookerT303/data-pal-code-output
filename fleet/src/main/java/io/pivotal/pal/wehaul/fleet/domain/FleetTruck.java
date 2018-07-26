package io.pivotal.pal.wehaul.fleet.domain;

import org.springframework.stereotype.Component;

import javax.persistence.*;

@Entity
@Table(name = "fleet_truck")
public class FleetTruck {

    @Id
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column
    private FleetTruckStatus status;

    @Column
    private Integer odometerReading;

    @Embedded
    private MakeModel makeModel;

    FleetTruck() {
        // default constructor
    }

    public void returnFromInspection(int odometerReading) {
        if (status != FleetTruckStatus.IN_INSPECTION) {
            throw new IllegalStateException("Truck is not currently in inspection");
        }
        if (this.odometerReading > odometerReading) {
            throw new IllegalArgumentException("Odometer reading cannot be less than previous reading");
        }

        this.status = FleetTruckStatus.INSPECTABLE;
        this.odometerReading = odometerReading;
    }

    public void sendForInspection() {
        if (status != FleetTruckStatus.INSPECTABLE) {
            throw new IllegalStateException("Truck cannot be inspected");
        }

        this.status = FleetTruckStatus.IN_INSPECTION;
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

    public void setVin(String vin) {
        this.vin = vin;
    }

    public FleetTruckStatus getStatus() {
        return status;
    }

    public void setStatus(FleetTruckStatus status) {
        this.status = status;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    public void setOdometerReading(Integer odometerReading) {
        this.odometerReading = odometerReading;
    }

    public MakeModel getMakeModel() {
        return makeModel;
    }

    @Override
    public String toString() {
        return "FleetTruck{" +
                "vin='" + vin + '\'' +
                ", status=" + status +
                ", odometerReading=" + odometerReading +
                ", makeModel=" + makeModel +
                '}';
    }

    @Component
    public static class Factory {

        private final TruckInfoLookupClient truckInfoLookupClient;

        public Factory(TruckInfoLookupClient truckInfoLookupClient) {
            this.truckInfoLookupClient = truckInfoLookupClient;
        }

        public FleetTruck buyTruck(String vin, int odometerReading) {
            if (odometerReading < 0) {
                throw new IllegalArgumentException("Cannot buy a truck with negative odometer reading");
            }
            FleetTruck truck = new FleetTruck();
            truck.vin = vin;
            truck.status = FleetTruckStatus.IN_INSPECTION;
            truck.odometerReading = odometerReading;
            truck.makeModel = truckInfoLookupClient.getMakeModelByVin(vin);

            return truck;
        }

    }
}
