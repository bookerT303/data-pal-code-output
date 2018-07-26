package io.pivotal.pal.wehaul.fleet.domain.command.event;

import java.util.Objects;

public class FleetTruckReturnedToYard implements FleetTruckEvent {

    private final String vin;
    private final int distanceTraveled;

    public FleetTruckReturnedToYard(String vin, int distanceTraveled) {
        this.vin = vin;
        this.distanceTraveled = distanceTraveled;
    }

    private FleetTruckReturnedToYard() {
        this.vin = null;
        this.distanceTraveled = -1;
    }

    public String getVin() {
        return vin;
    }

    public int getDistanceTraveled() {
        return distanceTraveled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FleetTruckReturnedToYard that = (FleetTruckReturnedToYard) o;
        return distanceTraveled == that.distanceTraveled &&
                Objects.equals(vin, that.vin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, distanceTraveled);
    }

    @Override
    public String toString() {
        return "FleetTruckReturnedToYard{" +
                "vin='" + vin + '\'' +
                ", distanceTraveled=" + distanceTraveled +
                '}';
    }
}
