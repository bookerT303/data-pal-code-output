package io.pivotal.pal.wehaul.rental.domain;

import io.pivotal.pal.wehaul.rental.domain.event.RentalTruckDroppedOff;
import io.pivotal.pal.wehaul.rental.domain.event.RentalTruckReserved;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.*;

@Entity
@Table(name = "rental_truck")
public class RentalTruck extends AbstractAggregateRoot {

    @Id
    @Column
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column
    private RentalTruckStatus status;

    @Enumerated(EnumType.STRING)
    @Column
    private RentalTruckSize size;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "truckVin")
    private Rental rental;

    public RentalTruck(String vin, RentalTruckSize truckSize) {
        this.vin = vin;
        this.status = RentalTruckStatus.RENTABLE;
        this.size = truckSize;
    }

    RentalTruck() {
        // default constructor
    }

    public void reserve(String customerName) {
        if (status != RentalTruckStatus.RENTABLE) {
            throw new IllegalStateException("Truck cannot be reserved");
        }

        this.status = RentalTruckStatus.RESERVED;
        this.rental = new Rental(customerName, this.vin);
        this.registerEvent(new RentalTruckReserved(this));
    }

    public void pickUp() {
        if (status != RentalTruckStatus.RESERVED) {
            throw new IllegalStateException("Only reserved trucks can be picked up");
        }

        this.status = RentalTruckStatus.RENTED;
    }

    public void dropOff(int distanceTraveled) {
        if (status != RentalTruckStatus.RENTED) {
            throw new IllegalStateException("Only rented trucks can be dropped off");
        }

        this.status = RentalTruckStatus.RENTABLE;
        this.rental = null;

        this.registerEvent(new RentalTruckDroppedOff(this, distanceTraveled));
    }

    public void preventRenting() {
        if (status != RentalTruckStatus.RENTABLE) {
            throw new IllegalStateException("Truck cannot be prevented from renting");
        }
        this.status = RentalTruckStatus.NOT_RENTABLE;
    }

    public void allowRenting() {
        if (status != RentalTruckStatus.NOT_RENTABLE) {
            throw new IllegalStateException("Truck is not rentable");
        }
        this.status = RentalTruckStatus.RENTABLE;
    }

    public String getVin() {
        return vin;
    }

    public RentalTruckStatus getStatus() {
        return status;
    }

    public RentalTruckSize getSize() {
        return size;
    }

    public Rental getRental() {
        return rental;
    }

    public static class Factory {

    }
}
