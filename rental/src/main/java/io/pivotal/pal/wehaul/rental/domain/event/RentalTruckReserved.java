package io.pivotal.pal.wehaul.rental.domain.event;

import io.pivotal.pal.wehaul.rental.domain.RentalTruck;

import java.time.LocalDateTime;
import java.util.Objects;

public class RentalTruckReserved {

    private final String vin;
    private final LocalDateTime createdDate;


    public RentalTruckReserved(RentalTruck truck) {
        this.vin = truck.getVin();
        this.createdDate = LocalDateTime.now();
    }

    public String getVin() {
        return vin;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RentalTruckReserved that = (RentalTruckReserved) o;
        return Objects.equals(vin, that.vin) &&
                Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, createdDate);
    }

    @Override
    public String toString() {
        return "RentalTruckReserved{" +
                "vin='" + vin + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
