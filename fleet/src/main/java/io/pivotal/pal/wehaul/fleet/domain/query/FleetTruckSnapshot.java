package io.pivotal.pal.wehaul.fleet.domain.query;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
public class FleetTruckSnapshot {

    @Id
    private String vin;

    @Column
    private String status;

    @Column
    private Integer odometerReading;

    @Column
    private String make;

    @Column
    private String model;

    public FleetTruckSnapshot() {
    }

    public FleetTruckSnapshot(String vin, String status, Integer odometerReading, String make, String model) {
        this.vin = vin;
        this.status = status;
        this.odometerReading = odometerReading;
        this.make = make;
        this.model = model;
    }

    public String getVin() {
        return vin;
    }

    public String getStatus() {
        return status;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FleetTruckSnapshot)) return false;
        FleetTruckSnapshot that = (FleetTruckSnapshot) o;
        return Objects.equals(vin, that.vin) &&
                Objects.equals(status, that.status) &&
                Objects.equals(odometerReading, that.odometerReading) &&
                Objects.equals(make, that.make) &&
                Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, status, odometerReading, make, model);
    }

    @Override
    public String toString() {
        return "FleetTruckSnapshot{" +
                "vin='" + vin + '\'' +
                ", status='" + status + '\'' +
                ", odometerReading=" + odometerReading +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
