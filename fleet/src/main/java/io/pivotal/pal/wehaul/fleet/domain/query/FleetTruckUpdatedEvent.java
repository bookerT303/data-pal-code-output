package io.pivotal.pal.wehaul.fleet.domain.query;

import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruck;

import java.util.Objects;

public class FleetTruckUpdatedEvent {

    private String vin;
    private String status;
    private Integer odometerReading;
    private String make;
    private String model;

    public FleetTruckUpdatedEvent(FleetTruck fleetTruck) {
        this.vin = fleetTruck.getVin();
        this.status = fleetTruck.getStatus().toString();
        this.odometerReading = fleetTruck.getOdometerReading();
        this.make = fleetTruck.getMakeModel().getMake();
        this.model = fleetTruck.getMakeModel().getModel();
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
        if (!(o instanceof FleetTruckUpdatedEvent)) return false;
        FleetTruckUpdatedEvent that = (FleetTruckUpdatedEvent) o;
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
        return "FleetTruckUpdatedEvent{" +
                "vin='" + vin + '\'' +
                ", status='" + status + '\'' +
                ", odometerReading=" + odometerReading +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
