package io.pivotal.pal.wehaul.rental.domain.event;

import io.pivotal.pal.wehaul.rental.domain.RentalTruck;

import java.time.LocalDateTime;
import java.util.Objects;

public class RentalTruckDroppedOff {

    private final String vin;
    private final int distanceTraveled;
    private final LocalDateTime createdDate;


    public RentalTruckDroppedOff(RentalTruck truck, int distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
        this.vin = truck.getVin();
        this.createdDate = LocalDateTime.now();
    }

    public String getVin() {
        return vin;
    }

    public int getDistanceTraveled() {
        return distanceTraveled;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RentalTruckDroppedOff that = (RentalTruckDroppedOff) o;
        return distanceTraveled == that.distanceTraveled &&
                Objects.equals(vin, that.vin) &&
                Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, distanceTraveled, createdDate);
    }

    @Override
    public String toString() {
        return "RentalTruckDroppedOff{" +
                "vin='" + vin + '\'' +
                ", distanceTraveled=" + distanceTraveled +
                ", createdDate=" + createdDate +
                '}';
    }
}
