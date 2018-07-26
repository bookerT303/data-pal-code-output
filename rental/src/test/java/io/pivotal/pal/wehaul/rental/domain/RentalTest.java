package io.pivotal.pal.wehaul.rental.domain;

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
        assertThat(rental.getDistanceTraveled()).isEqualTo(null);
    }

    @Test
    public void pickUp() {
        Rental rental = Rental.createRental("some-customer-name", "test-0001");

        rental.pickUp();

        assertThat(rental.getDistanceTraveled()).isEqualTo(0);
    }

    @Test
    public void dropOff() {
        Rental rental = Rental.createRental("some-customer-name", "test-0001");
        rental.pickUp();

        rental.dropOff(2000);

        assertThat(rental.getDistanceTraveled()).isEqualTo(2000);
    }

    @Test
    public void pickUp_alreadyPickedUp() {
        Rental rental = Rental.createRental("some-customer-name", "test-0001");
        rental.pickUp();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rental.pickUp())
                .withMessage("Rental has already been picked up");
    }

    @Test
    public void dropOff_notPickedUp() {
        Rental rental = Rental.createRental("some-customer-name", "test-0001");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rental.dropOff(0))
                .withMessage("Cannot drop off before picking up rental");
    }

    @Test
    public void dropOff_alreadyDroppedOff() {
        Rental rental = Rental.createRental("some-customer-name", "test-0001");
        rental.pickUp();
        rental.dropOff(1);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rental.dropOff(1))
                .withMessage("Rental is already dropped off");
    }
}
