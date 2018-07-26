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

    @Enumerated(EnumType.STRING)
    @Column
    private RentalTruckSize size;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "truckVin")
    private Rental rental;

    RentalTruck() {
        // default constructor
    }

    public void reserve(String customerName) {
        if (status != RentalTruckStatus.RENTABLE) {
            throw new IllegalStateException("Truck cannot be reserved");
        }

        this.status = RentalTruckStatus.RESERVED;
        this.rental = new Rental(customerName, this.vin);
    }

    public void pickUp() {
        if (status != RentalTruckStatus.RESERVED) {
            throw new IllegalStateException("Only reserved trucks can be picked up");
        }

        this.status = RentalTruckStatus.RENTED;
    }

    public void dropOff() {
        if (status != RentalTruckStatus.RENTED) {
            throw new IllegalStateException("Only rented trucks can be dropped off");
        }

        this.status = RentalTruckStatus.RENTABLE;
        this.rental = null;
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

        private final TruckSizeLookupClient truckSizeLookupClient;

        public Factory(TruckSizeLookupClient truckSizeLookupClient) {
            this.truckSizeLookupClient = truckSizeLookupClient;
        }

        public RentalTruck createRentableTruck(String vin, String make, String model) {
            RentalTruck truck = new RentalTruck();
            truck.vin = vin;
            truck.status = RentalTruckStatus.RENTABLE;
            truck.size = truckSizeLookupClient.getSizeByMakeModel(make, model);
            return truck;
        }
    }
}
