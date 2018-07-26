package io.pivotal.pal.wehaul.fleet.domain.event;

import java.util.Objects;

public class FleetTruckReturnedFromInspection implements FleetTruckEvent {

    private final String vin;
    private final Integer odometerReading;
    private final String notes;

    public FleetTruckReturnedFromInspection(String vin, Integer odometerReading, String notes) {
        this.vin = vin;
        this.odometerReading = odometerReading;
        this.notes = notes;
    }

    private FleetTruckReturnedFromInspection() {
        this.vin = null;
        this.odometerReading = null;
        this.notes = null;
    }

    public String getVin() {
        return vin;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FleetTruckReturnedFromInspection that = (FleetTruckReturnedFromInspection) o;
        return Objects.equals(vin, that.vin) &&
                Objects.equals(odometerReading, that.odometerReading) &&
                Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, odometerReading, notes);
    }

    @Override
    public String toString() {
        return "FleetTruckReturnedFromInspection{" +
                "vin='" + vin + '\'' +
                ", odometerReading=" + odometerReading +
                ", notes='" + notes + '\'' +
                '}';
    }
}
