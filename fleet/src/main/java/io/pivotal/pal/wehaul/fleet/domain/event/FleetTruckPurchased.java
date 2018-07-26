package io.pivotal.pal.wehaul.fleet.domain.event;

import java.util.Objects;

public class FleetTruckPurchased implements FleetTruckEvent {

    private final String vin;
    private final String make;
    private final String model;
    private final Integer odometerReading;

    public FleetTruckPurchased(String vin, String make, String model, Integer odometerReading) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.odometerReading = odometerReading;
    }

    private FleetTruckPurchased() {
        this.vin = null;
        this.make = null;
        this.model = null;
        this.odometerReading = null;
    }

    public String getVin() {
        return vin;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FleetTruckPurchased that = (FleetTruckPurchased) o;
        return Objects.equals(vin, that.vin) &&
                Objects.equals(make, that.make) &&
                Objects.equals(model, that.model) &&
                Objects.equals(odometerReading, that.odometerReading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, make, model, odometerReading);
    }

    @Override
    public String toString() {
        return "FleetTruckPurchased{" +
                "vin='" + vin + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", odometerReading=" + odometerReading +
                '}';
    }
}
