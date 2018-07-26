package io.pivotal.pal.wehaul.fleet.domain.command.event;

import java.util.Objects;

public class FleetTruckRemovedFromYard implements FleetTruckEvent {

    private final String vin;

    public FleetTruckRemovedFromYard(String vin) {
        this.vin = vin;
    }

    private FleetTruckRemovedFromYard() {
        this.vin = null;
    }

    public String getVin() {
        return vin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FleetTruckRemovedFromYard that = (FleetTruckRemovedFromYard) o;
        return Objects.equals(vin, that.vin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin);
    }

    @Override
    public String toString() {
        return "FleetTruckRemovedFromYard{" +
                "vin='" + vin + '\'' +
                '}';
    }
}
