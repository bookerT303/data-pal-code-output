package io.pivotal.pal.wehaul.fleet.controller;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.service.FleetTruckService;
import io.pivotal.pal.wehaul.rental.service.RentalService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FleetTruckControllerRefactorTest {

    @Mock
    private FleetTruckService fleetTruckService;

    @Mock
    private RentalService rentalService;

    private FleetTruckController controller;

    @Before
    public void setUp() {
        controller = new FleetTruckController(fleetTruckService, rentalService);
    }

    @Test
    public void buyTruck() {
        String vin = "vin-01";
        int odometerReading = 42;

        ResponseEntity<Void> response = controller.buyTruck(new FleetTruckController.BuyTruckDto(vin, odometerReading));

        verify(fleetTruckService).buyTruck(vin, odometerReading);
        verify(rentalService).createRentableTruck(vin);
        verify(rentalService).removeRentableTruck(vin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getAllTrucks() {
        List<FleetTruck> trucks = Collections.singletonList(mock(FleetTruck.class));
        when(fleetTruckService.findAll()).thenReturn(trucks);

        ResponseEntity<Collection<FleetTruck>> response = controller.getAllTrucks();

        verify(fleetTruckService, times(1)).findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(trucks);
    }

    @Test
    public void sendForInspection() {
        String vin = "vin-01";

        ResponseEntity<Void> response = controller.sendForInspection(vin);

        verify(fleetTruckService).sendForInspection(vin);
        verify(rentalService).removeRentableTruck(vin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void returnFromInspection() {
        String vin = "vin-01";
        int odometerReading = 42;
        String notes = "notes";

        ResponseEntity<Void> response = controller.returnFromInspection(vin, new FleetTruckController.ReturnFromInspectionDto(notes, odometerReading));

        verify(fleetTruckService).returnFromInspection(vin, notes, odometerReading);
        verify(rentalService).addRentableTruck(vin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
