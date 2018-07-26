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

    @Column(nullable = false)
    private String truckVin;

    Rental() {
        // default constructor
    }

    public Rental(String customerName, String truckVin) {
        this.customerName = customerName;
        this.confirmationNumber = UUID.randomUUID();
        this.truckVin = truckVin;
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

    @Override
    public String toString() {
        return "Rental{" +
                "confirmationNumber=" + confirmationNumber +
                ", customerName='" + customerName + '\'' +
                ", truckVin='" + truckVin + '\'' +
                '}';
    }
}
