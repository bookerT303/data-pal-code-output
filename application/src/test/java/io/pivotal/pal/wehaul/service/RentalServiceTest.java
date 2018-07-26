package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.rental.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RentalServiceTest {

    @Mock
    private RentalTruckRepository mockTruckRepository;
    @Mock
    private TruckSizeLookupClient mockTruckSizeLookupClient;
    @Captor
    private ArgumentCaptor<RentalTruck> truckCaptor;

    private RentalTruck.Factory rentalTruckFactory;
    @Mock
    private RentalTruck.Factory mockRentalTruckFactory;
    private RentalService rentalService;

    @Before
    public void setUp() {
        rentalTruckFactory = new RentalTruck.Factory(mockTruckSizeLookupClient);
        rentalService = new RentalService(mockTruckRepository, mockRentalTruckFactory);
    }

    @Test
    public void createRental() {
        RentalTruck truck = rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);
        String customerName = "some-customer-name";


        rentalService.reserve(customerName);


        verify(mockTruckRepository).findTop1ByStatus(RentalTruckStatus.RENTABLE);
        verify(mockTruckRepository).save(truckCaptor.capture());

        RentalTruck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);

        Rental savedRental = truckCaptor.getValue().getRental();
        assertThat(savedRental.getConfirmationNumber()).isNotNull();
        assertThat(savedRental.getTruckVin()).isEqualTo(truck.getVin());
    }

    @Test
    public void createRental_whenNoTruckAvailable() {
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rentalService.reserve("some-customer-name"))
                .withMessage("No trucks available to rent");

        verify(mockTruckRepository).findTop1ByStatus(RentalTruckStatus.RENTABLE);
        verifyNoMoreInteractions(mockTruckRepository);
    }

    @Test
    public void pickUp() {
        RentalTruck truck = rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
        String customerName = "some-customer-name";
        truck.reserve(customerName);
        when(mockTruckRepository.findOneByRentalConfirmationNumber(any())).thenReturn(truck);


        Rental rental = truck.getRental();
        rentalService.pickUp(rental.getConfirmationNumber());


        verify(mockTruckRepository).findOneByRentalConfirmationNumber(rental.getConfirmationNumber());
        verify(mockTruckRepository).save(truck);
    }

    @Test
    public void pickUp_whenNoRentalFound() {
        when(mockTruckRepository.findOneByRentalConfirmationNumber(any())).thenReturn(null);

        UUID confirmationNumber = UUID.randomUUID();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.pickUp(confirmationNumber))
                .withMessage(String.format("No rental found for id=%s", confirmationNumber));

        verify(mockTruckRepository).findOneByRentalConfirmationNumber(confirmationNumber);
        verifyNoMoreInteractions(mockTruckRepository);
    }

    @Test
    public void dropOff() {
        RentalTruck truck = rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
        String customerName = "some-customer-name";
        truck.reserve(customerName);
        truck.pickUp();
        when(mockTruckRepository.findOneByRentalConfirmationNumber(any())).thenReturn(truck);


        rentalService.dropOff(truck.getRental().getConfirmationNumber(), 100);


        verify(mockTruckRepository).save(truckCaptor.capture());
    }

    @Test
    public void dropOff_whenNoRentalFound() {
        when(mockTruckRepository.findOneByRentalConfirmationNumber(any())).thenReturn(null);


        UUID confirmationNumber = UUID.randomUUID();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.dropOff(confirmationNumber, 100))
                .withMessage(String.format("No rental found for id=%s", confirmationNumber));


        verify(mockTruckRepository).findOneByRentalConfirmationNumber(confirmationNumber);
        verifyNoMoreInteractions(mockTruckRepository);
    }

    @Test
    public void addTruck() {
        String vin = "test-0001";
        String make = "test-make";
        String model = "test-model";
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockRentalTruckFactory.createRentableTruck(anyString(), anyString(), anyString()))
                .thenReturn(mockTruck);


        rentalService.addTruck(vin, make, model);


        verify(mockRentalTruckFactory).createRentableTruck(vin, make, model);
        verify(mockTruck).preventRenting();
        verify(mockTruckRepository).save(mockTruck);
    }

    @Test
    public void preventRenting() {
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn("best-vin");

        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);


        rentalService.preventRenting("best-vin");


        verify(mockTruckRepository).findOne("best-vin");
        verify(mockTruck).preventRenting();
        verify(mockTruckRepository).save(mockTruck);
    }

    @Test
    public void preventRenting_whenTruckNotFound() {
        when(mockTruckRepository.findOne(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.preventRenting("vin"))
                .withMessage("No truck found with vin=vin");
    }

    @Test
    public void allowRenting() {
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn("best-vin");

        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);


        rentalService.allowRenting("best-vin");


        verify(mockTruckRepository).findOne("best-vin");
        verify(mockTruck).allowRenting();
        verify(mockTruckRepository).save(mockTruck);
    }

    @Test
    public void allowRenting_whenTruckNotFound() {
        when(mockTruckRepository.findOne(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.allowRenting("vin"))
                .withMessage("No truck found with vin=vin");
    }
}
