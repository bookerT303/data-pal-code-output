package io.pivotal.pal.wehaul.domain;

import javax.persistence.*;

@Entity
@Table
public class Truck {

    @Id
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column
    private TruckStatus status;

    @Column
    private Integer odometerReading;

    public Truck(String vin, int odometerReading) {
        this.vin = vin;
        this.odometerReading = odometerReading;
        this.status = TruckStatus.IN_INSPECTION;
    }

    Truck() {
        // default constructor
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public TruckStatus getStatus() {
        return status;
    }

    public void setStatus(TruckStatus status) {
        this.status = status;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    public void setOdometerReading(Integer odometerReading) {
        this.odometerReading = odometerReading;
    }

    @Override
    public String toString() {
        return "Truck{" +
            "vin=" + vin +
            ", status=" + status +
            ", odometerReading=" + odometerReading +
            '}';
    }
}
