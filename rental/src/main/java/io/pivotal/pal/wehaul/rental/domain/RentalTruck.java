package io.pivotal.pal.wehaul.rental.domain;

import javax.persistence.*;

@Entity
@Table(name = "rental_truck")
public class RentalTruck {

    @Id
    @Column
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column
    private RentalTruckStatus status;

    public RentalTruck() {
        // default constructor
    }

    public static RentalTruck createRentableTruck(String vin) {
        RentalTruck truck = new RentalTruck();
        truck.vin = vin;
        truck.status = RentalTruckStatus.RENTABLE;

        return truck;
    }

    public void reserve() {
        // TODO: implement me
    }

    public void pickUp() {
        // TODO: implement me
    }

    public void dropOff() {
        // TODO: implement me
    }

    public void preventRenting() {
        // TODO: implement me
    }

    public void allowRenting() {
        // TODO: implement me
    }

    public String getVin() {
        return vin;
    }

    public RentalTruckStatus getStatus() {
        return status;
    }
}
