package io.pivotal.pal.wehaul.fleet.domain;

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

    FleetTruck() {
        // default constructor
    }

    public static FleetTruck buyTruck(String vin, int odometerReading) {
        if (odometerReading < 0) {
            throw new IllegalArgumentException("Cannot buy a truck with negative odometer reading");
        }
        FleetTruck truck = new FleetTruck();
        truck.vin = vin;
        truck.status = FleetTruckStatus.IN_INSPECTION;
        truck.odometerReading = odometerReading;

        return truck;
    }

    public void returnFromInspection(int odometerReading) {
        // TODO: implement me
    }

    public void sendForInspection() {
        // TODO: implement me
    }

    public void removeFromYard() {
        // TODO: implement me
    }

    public void returnToYard(int distanceTraveled) {
        // TODO: implement me
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

    public void setOdometerReading(Integer odometerReading) {
        this.odometerReading = odometerReading;
    }

    @Override
    public String toString() {
        return "FleetTruck{" +
                "vin=" + vin +
                ", status=" + status +
                ", odometerReading=" + odometerReading +
                '}';
    }
}
