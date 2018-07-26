package io.pivotal.pal.wehaul.fleet.domain.command.event;

import java.util.Objects;

public class FleetTruckSentForInspection implements FleetTruckEvent {

    private final String vin;

    public FleetTruckSentForInspection(String vin) {
        this.vin = vin;
    }

    private FleetTruckSentForInspection() {
        this.vin = null;
    }

    public String getVin() {
        return vin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FleetTruckSentForInspection that = (FleetTruckSentForInspection) o;
        return Objects.equals(vin, that.vin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin);
    }

    @Override
    public String toString() {
        return "FleetTruckSentForInspection{" +
                "vin='" + vin + '\'' +
                '}';
    }
}
