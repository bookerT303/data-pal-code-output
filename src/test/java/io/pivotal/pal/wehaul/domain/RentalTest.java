package io.pivotal.pal.wehaul.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class RentalTest {

    @Test
    public void createRental() {
        String customerName = "some-customer-name";
        String truckVin = "test-0001";
        Rental rental = Rental.createRental(customerName, truckVin);

        assertThat(rental.getConfirmationNumber()).isNotNull();
        assertThat(rental.getTruckVin()).isEqualTo(truckVin);
        assertThat(rental.getDistanceTraveled()).isNull();
    }

    @Test
    public void pickUp() {
        String customerName = "some-customer-name";
        String truckVin = "test-0001";
        Rental rental = Rental.createRental(customerName, truckVin);
        rental.pickUp();

        assertThat(rental.getDistanceTraveled()).isEqualTo(0);
    }

    @Test
    public void dropOff() {
        String customerName = "some-customer-name";
        String truckVin = "test-0001";
        Rental rental = Rental.createRental(customerName, truckVin);
        rental.pickUp();

        int distanceTraveled = 2000;
        rental.dropOff(distanceTraveled);

        assertThat(rental.getDistanceTraveled()).isEqualTo(distanceTraveled);
    }

    @Test
    public void pickUp_alreadyPickedUp() {
        String customerName = "some-customer-name";
        String truckVin = "test-0001";
        Rental rental = Rental.createRental(customerName, truckVin);
        rental.pickUp();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rental.pickUp())
                .withMessage("Rental has already been picked up");
    }

    @Test
    public void dropOff_notPickedUp() {
        String customerName = "some-customer-name";
        String truckVin = "test-0001";
        Rental rental = Rental.createRental(customerName, truckVin);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rental.dropOff(0))
                .withMessage("Cannot drop off before picking up rental");
    }

    @Test
    public void dropOff_alreadyDroppedOff() {
        String customerName = "some-customer-name";
        String truckVin = "test-0001";
        Rental rental = Rental.createRental(customerName, truckVin);
        rental.pickUp();
        rental.dropOff(10);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rental.dropOff(11))
                .withMessage("Rental is already dropped off");
    }
}
