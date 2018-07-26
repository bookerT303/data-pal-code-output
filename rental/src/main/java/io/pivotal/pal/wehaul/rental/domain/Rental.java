package io.pivotal.pal.wehaul.rental.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rental")
public class Rental {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID confirmationNumber;

    @Column(nullable = false)
    private String customerName;

    @Column
    private String truckVin;

    @Column
    private Integer distanceTraveled;

    Rental() {
        // default constructor
    }

    public static Rental createRental(String customerName,
                                      String truckVin) {
        Rental reservation = new Rental();
        reservation.customerName = customerName;
        reservation.confirmationNumber = UUID.randomUUID();
        reservation.truckVin = truckVin;

        return reservation;
    }

    public void pickUp() {
        if (distanceTraveled != null) {
            throw new IllegalStateException("Rental has already been picked up");
        }

        distanceTraveled = 0;
    }

    public void dropOff(int distanceTraveled) {
        if (this.distanceTraveled == null) {
            throw new IllegalStateException("Cannot drop off before picking up rental");
        }
        if (this.distanceTraveled != 0) {
            throw new IllegalStateException("Rental is already dropped off");
        }

        this.distanceTraveled = distanceTraveled;
    }

    public UUID getConfirmationNumber() {
        return confirmationNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getTruckVin() {
        return truckVin;
    }

    public Integer getDistanceTraveled() {
        return distanceTraveled;
    }

    @Override
    public String toString() {
        return "Rental{" +
                "confirmationNumber=" + confirmationNumber +
                ", customerName='" + customerName + '\'' +
                ", truckVin='" + truckVin + '\'' +
                ", distanceTraveled=" + distanceTraveled +
                '}';
    }
}
