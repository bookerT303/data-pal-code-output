package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.rental.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
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
    @Mock
    private RentalTruck.Factory mockTruckFactory;
    @Captor
    private ArgumentCaptor<Rental> rentalCaptor;
    @Captor
    private ArgumentCaptor<RentalTruck> truckCaptor;

    private RentalService rentalService;

    @Before
    public void setUp() {
        rentalService = new RentalService(mockRentalRepository, mockTruckRepository, mockTruckFactory);
    }

    @Test
    public void createRental() {
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn("some-vin");
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(mockTruck);

        rentalService.createRental("some-customer-name");

        InOrder inOrder = inOrder(mockTruck, mockTruckRepository, mockRentalRepository);
        inOrder.verify(mockTruckRepository).findTop1ByStatus(RentalTruckStatus.RENTABLE);
        inOrder.verify(mockTruck).reserve();
        inOrder.verify(mockTruckRepository).save(truckCaptor.capture());
        inOrder.verify(mockRentalRepository).save(rentalCaptor.capture());

        RentalTruck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck).isSameAs(mockTruck);

        Rental savedRental = rentalCaptor.getValue();
        assertThat(savedRental.getCustomerName()).isEqualTo("some-customer-name");
        assertThat(savedRental.getTruckVin()).isEqualTo("some-vin");
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
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);

        Rental mockRental = mock(Rental.class);
        when(mockRentalRepository.findOne(any())).thenReturn(mockRental);

        rentalService.pickUp(mockRental.getConfirmationNumber());

        InOrder inOrder = inOrder(mockRental, mockRentalRepository, mockTruck, mockTruckRepository);
        inOrder.verify(mockRentalRepository).findOne(mockRental.getConfirmationNumber());
        inOrder.verify(mockRental).pickUp();
        inOrder.verify(mockRentalRepository).save(mockRental);

        inOrder.verify(mockTruckRepository).findOne(mockTruck.getVin());
        inOrder.verify(mockTruck).pickUp();
        inOrder.verify(mockTruckRepository).save(mockTruck);
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
        Rental mockRental = mock(Rental.class);
        when(mockRentalRepository.findOne(any())).thenReturn(mockRental);

        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);

        UUID confirmationNumber = UUID.randomUUID();
        int distanceTraveled = 500;
        rentalService.dropOff(confirmationNumber, distanceTraveled);


        InOrder inOrder = inOrder(mockRental, mockRentalRepository, mockTruck, mockTruckRepository);
        inOrder.verify(mockRentalRepository).findOne(confirmationNumber);
        inOrder.verify(mockRental).dropOff(distanceTraveled);
        inOrder.verify(mockRentalRepository).save(rentalCaptor.capture());
        Rental savedRental = rentalCaptor.getValue();
        assertThat(savedRental).isSameAs(mockRental);

        inOrder.verify(mockTruckRepository).findOne(any());
        inOrder.verify(mockTruck).dropOff();
        inOrder.verify(mockTruckRepository).save(truckCaptor.capture());
        RentalTruck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck).isSameAs(mockTruck);
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
    public void addTruck() {
        RentalTruck mockTruck = mock(RentalTruck.class);

        when(mockTruckFactory.createRentableTruck(any(), any(), any()))
                .thenReturn(mockTruck);

        String vin = "cool-vin";
        String make = "make";
        String model = "model";
        rentalService.addTruck(vin, make, model);

        verify(mockTruckFactory).createRentableTruck(vin, make, model);
        verify(mockTruck).preventRenting();
        verify(mockTruckRepository).save(mockTruck);
    }

    @Test
    public void preventRenting() {
        String vin = "best-vin";

        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn(vin);

        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);


        rentalService.preventRenting(vin);


        verify(mockTruckRepository).findOne(vin);
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
        String vin = "best-vin";

        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn(vin);

        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);


        rentalService.allowRenting(vin);


        verify(mockTruckRepository).findOne(vin);
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