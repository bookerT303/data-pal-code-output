package io.pivotal.pal.wehaul.domain;

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

    private Integer distanceTraveled;

    Rental() {
        // default constructor
    }

    public Rental(String customerName,
                  String truckVin) {
        this.customerName = customerName;
        this.truckVin = truckVin;
        this.confirmationNumber = UUID.randomUUID();
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

    public void setDistanceTraveled(Integer distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
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
