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
    private RentalService rentalService;

    @Before
    public void setUp() {
        rentalTruckFactory = new RentalTruck.Factory(mockTruckSizeLookupClient);
        rentalService = new RentalService(
                mockTruckRepository,
                rentalTruckFactory
        );
    }

    @Test
    public void createRental() {
        RentalTruck truck = rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);
        String customerName = "some-customer-name";


        rentalService.createRental(customerName);


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
                .isThrownBy(() -> rentalService.createRental("some-customer-name"))
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


        rentalService.dropOff(truck.getRental().getConfirmationNumber());


        verify(mockTruckRepository).save(truckCaptor.capture());
    }

    @Test
    public void dropOff_whenNoRentalFound() {
        when(mockTruckRepository.findOneByRentalConfirmationNumber(any())).thenReturn(null);


        UUID confirmationNumber = UUID.randomUUID();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.dropOff(confirmationNumber))
                .withMessage(String.format("No rental found for id=%s", confirmationNumber));


        verify(mockTruckRepository).findOneByRentalConfirmationNumber(confirmationNumber);
        verifyNoMoreInteractions(mockTruckRepository);
    }

    @Test
    public void createRentableTruck() {
        when(mockTruckSizeLookupClient.getSizeByMakeModel(any(), any())).thenReturn(RentalTruckSize.LARGE);

        rentalService.createRentableTruck("cool-vin", "make", "model");

        verify(mockTruckRepository).save(truckCaptor.capture());
        assertThat(truckCaptor.getValue()).isNotNull();
        assertThat(truckCaptor.getValue().getVin()).isEqualToIgnoringCase("cool-vin");
        assertThat(truckCaptor.getValue().getSize()).isEqualTo(RentalTruckSize.LARGE);

        verify(mockTruckSizeLookupClient).getSizeByMakeModel("make", "model");
    }

    @Test
    public void removeRentableTruck() {
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn("best-vin");

        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);


        rentalService.removeRentableTruck("best-vin");


        verify(mockTruckRepository).findOne("best-vin");
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
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn("best-vin");

        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);


        rentalService.addRentableTruck("best-vin");


        verify(mockTruckRepository).findOne("best-vin");
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