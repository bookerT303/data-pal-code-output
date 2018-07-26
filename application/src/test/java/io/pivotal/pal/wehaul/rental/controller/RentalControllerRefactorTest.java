package io.pivotal.pal.wehaul.rental.controller;

import io.pivotal.pal.wehaul.fleet.service.FleetTruckService;
import io.pivotal.pal.wehaul.rental.domain.Rental;
import io.pivotal.pal.wehaul.rental.service.RentalService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RentalControllerRefactorTest {

    @Mock
    RentalService rentalService;

    @Mock
    FleetTruckService fleetTruckService;

    private RentalController controller;

    @Before
    public void setUp() throws Exception {
        controller = new RentalController(rentalService, fleetTruckService);
    }

    @Test
    public void createRental() {
        String name = "My Name";
        String newVin = "vin-01";

        when(rentalService.createRental(name)).thenReturn(Rental.createRental("_", newVin));

        ResponseEntity<Void> response = controller.createRental(new RentalController.CreateRentalDto(name));

        verify(rentalService).createRental(name);
        verify(fleetTruckService).removeFromYard(newVin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void pickUpRental() {
        UUID rentalId = UUID.randomUUID();
        ResponseEntity<Void> response = controller.pickUpRental(rentalId);

        verify(rentalService).pickUp(rentalId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void dropOffRental() {
        UUID rentalId = UUID.randomUUID();
        String vin = "vin-01";
        when(rentalService.dropOff(any(UUID.class), anyInt())).thenReturn(Rental.createRental("name", vin));

        ResponseEntity<Void> response = controller.dropOffRental(rentalId, new RentalController.DropOffRentalDto(42));

        verify(rentalService).dropOff(rentalId, 42);
        verify(fleetTruckService).returnToYard(vin, 42);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
