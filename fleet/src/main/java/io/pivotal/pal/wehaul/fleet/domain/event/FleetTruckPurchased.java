package io.pivotal.pal.wehaul.fleet.domain.event;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;

import java.time.LocalDateTime;
import java.util.Objects;

public class FleetTruckPurchased {

    private final String vin;
    private final String make;
    private final String model;
    private final LocalDateTime createdDate;

    public FleetTruckPurchased(FleetTruck fleetTruck) {
        this.vin = fleetTruck.getVin();
        this.make = fleetTruck.getMakeModel().getMake();
        this.model = fleetTruck.getMakeModel().getModel();
        this.createdDate = LocalDateTime.now();
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FleetTruckPurchased that = (FleetTruckPurchased) o;
        return Objects.equals(vin, that.vin) &&
                Objects.equals(make, that.make) &&
                Objects.equals(model, that.model) &&
                Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, make, model, createdDate);
    }

    @Override
    public String toString() {
        return "FleetTruckPurchased{" +
                "vin='" + vin + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
