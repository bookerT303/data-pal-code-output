package io.pivotal.pal.wehaul.rental.service;

import io.pivotal.pal.wehaul.rental.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@DirtiesContext
@RunWith(MockitoJUnitRunner.class)
public class RentalServiceTest {

    @Mock
    private RentalRepository mockRentalRepository;
    @Mock
    private RentalTruckRepository mockTruckRepository;
    @Captor
    private ArgumentCaptor<Rental> rentalCaptor;
    @Captor
    private ArgumentCaptor<RentalTruck> truckCaptor;

    private RentalService rentalService;

    @Before
    public void setUp() {
        rentalService = new RentalService(mockRentalRepository, mockTruckRepository);
    }

    @Test
    public void createRental() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);

        Rental rental = Rental.createRental("some-customer-name", truck.getVin());
        when(mockRentalRepository.findOne(any())).thenReturn(rental);


        rentalService.createRental(rental.getCustomerName());


        verify(mockTruckRepository).findTop1ByStatus(RentalTruckStatus.RENTABLE);
        verify(mockTruckRepository).save(truckCaptor.capture());
        verify(mockRentalRepository).save(rentalCaptor.capture());

        RentalTruck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);

        Rental savedRental = rentalCaptor.getValue();
        assertThat(savedRental.getConfirmationNumber()).isNotNull();
        assertThat(savedRental.getTruckVin()).isEqualTo(truck.getVin());
    }

    @Test
    public void createRental_whenNoTruckAvailable() {
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rentalService.createRental("some-customer-name"))
                .withMessage("No trucks available to rent");

        verify(mockTruckRepository).findTop1ByStatus(RentalTruckStatus.RENTABLE);
        verifyNoMoreInteractions(mockTruckRepository);

        verifyZeroInteractions(mockRentalRepository);
    }

    @Test
    public void pickUp() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        Rental rental = Rental.createRental("some-customer-name", truck.getVin());
        when(mockRentalRepository.findOne(any())).thenReturn(rental);

        rentalService.createRental(rental.getCustomerName());


        rentalService.pickUp(rental.getConfirmationNumber());


        assertThat(rental.getDistanceTraveled()).isEqualTo(0);

        verify(mockRentalRepository).findOne(rental.getConfirmationNumber());
        verify(mockRentalRepository).save(rental);

        verify(mockTruckRepository).findOne(truck.getVin());
    }

    @Test
    public void pickUp_whenNoRentalFound() {
        when(mockRentalRepository.findOne(any())).thenReturn(null);

        UUID rentalId = UUID.randomUUID();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.pickUp(rentalId))
                .withMessage(String.format("No rental found for id=%s", rentalId));

        verify(mockRentalRepository).findOne(rentalId);
        verifyNoMoreInteractions(mockRentalRepository);
    }

    @Test
    public void dropOff() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        Rental rental = Rental.createRental("some-customer-name", truck.getVin());
        when(mockRentalRepository.findOne(any())).thenReturn(rental);

        rentalService.createRental("some-customer-name");
        rentalService.pickUp(rental.getConfirmationNumber());


        int distanceTraveled = 1000000;
        rentalService.dropOff(rental.getConfirmationNumber(), distanceTraveled);


        assertThat(rental.getDistanceTraveled()).isEqualTo(distanceTraveled);

        verify(mockTruckRepository, times(3)).save(truckCaptor.capture());
        verify(mockRentalRepository, times(3)).save(rentalCaptor.capture());

        RentalTruck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void dropOff_whenNoRentalFound() {
        when(mockRentalRepository.findOne(any())).thenReturn(null);

        UUID rentalId = UUID.randomUUID();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.dropOff(rentalId, 0))
                .withMessage(String.format("No rental found for id=%s", rentalId));

        verify(mockRentalRepository).findOne(rentalId);
        verifyNoMoreInteractions(mockRentalRepository);
        verifyZeroInteractions(mockTruckRepository);
    }

    @Test
    public void createRentableTruck() {
        String vin = "cool-vin";

        rentalService.createRentableTruck(vin);

        verify(mockTruckRepository).save(truckCaptor.capture());
        assertThat(truckCaptor.getValue()).isNotNull();
        assertThat(truckCaptor.getValue().getVin()).isEqualToIgnoringCase(vin);
    }

    @Test
    public void removeRentableTruck() {
        String vin = "best-vin";

        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn(vin);

        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);


        rentalService.removeRentableTruck(vin);


        verify(mockTruckRepository).findOne(vin);
        verify(mockTruck).preventRenting();
        verify(mockTruckRepository).save(mockTruck);
    }

    @Test
    public void removeRentableTruck_whenTruckNotFound() {
        when(mockTruckRepository.findOne(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.removeRentableTruck("vin"))
                .withMessage("No truck found with vin=vin");
    }

    @Test
    public void addRentableTruck() {
        String vin = "best-vin";

        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn(vin);

        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);


        rentalService.addRentableTruck(vin);


        verify(mockTruckRepository).findOne(vin);
        verify(mockTruck).allowRenting();
        verify(mockTruckRepository).save(mockTruck);
    }

    @Test
    public void addRentableTruck_whenTruckNotFound() {
        when(mockTruckRepository.findOne(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.addRentableTruck("vin"))
                .withMessage("No truck found with vin=vin");
    }
}