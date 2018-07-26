package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RentalServiceTest {

    @Mock
    private RentalRepository mockRentalRepository;
    @Mock
    private TruckRepository mockTruckRepository;
    @Captor
    private ArgumentCaptor<Rental> rentalCaptor;
    @Captor
    private ArgumentCaptor<Truck> truckCaptor;

    private RentalService rentalService;

    @Before
    public void setUp() {
        rentalService = new RentalService(mockRentalRepository, mockTruckRepository);
    }

    @Test
    public void create() {
        Truck mockTruck = mock(Truck.class);
        when(mockTruck.getVin()).thenReturn("some-vin");
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(mockTruck);

        rentalService.createRental("some-customer-name");

        InOrder inOrder = inOrder(mockTruck, mockTruckRepository, mockRentalRepository);
        inOrder.verify(mockTruckRepository).findTop1ByStatus(TruckStatus.RENTABLE);
        inOrder.verify(mockTruck).reserve();
        inOrder.verify(mockTruckRepository).save(truckCaptor.capture());
        inOrder.verify(mockRentalRepository).save(rentalCaptor.capture());

        Truck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck).isSameAs(mockTruck);

        Rental savedRental = rentalCaptor.getValue();
        assertThat(savedRental.getCustomerName()).isEqualTo("some-customer-name");
        assertThat(savedRental.getTruckVin()).isEqualTo("some-vin");
    }

    @Test
    public void create_whenNoTruckRentable() {
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rentalService.createRental("some-customer-name"))
                .withMessage("No trucks available to rent");

        verify(mockTruckRepository).findTop1ByStatus(TruckStatus.RENTABLE);
        verifyNoMoreInteractions(mockTruckRepository);

        verifyZeroInteractions(mockRentalRepository);
    }

    @Test
    public void pickUp() {
        Truck mockTruck = mock(Truck.class);
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

        UUID confirmationNumber = UUID.randomUUID();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.pickUp(confirmationNumber))
                .withMessage(String.format("No rental found for id=%s", confirmationNumber));

        verify(mockRentalRepository).findOne(confirmationNumber);
        verifyNoMoreInteractions(mockRentalRepository);
    }

    @Test
    public void dropOff() {
        Rental mockRental = mock(Rental.class);
        when(mockRentalRepository.findOne(any())).thenReturn(mockRental);

        Truck mockTruck = mock(Truck.class);
        when(mockTruck.getOdometerReading()).thenReturn(10_000);
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
        inOrder.verify(mockTruck).returnToService(10_000 + 500);
        inOrder.verify(mockTruckRepository).save(truckCaptor.capture());
        Truck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck).isSameAs(mockTruck);
    }

    @Test
    public void dropOff_whenNoRentalFound() {
        when(mockRentalRepository.findOne(any())).thenReturn(null);

        UUID confirmationNumber = UUID.randomUUID();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.dropOff(confirmationNumber, 0))
                .withMessage(String.format("No rental found for id=%s", confirmationNumber));

        verify(mockRentalRepository).findOne(confirmationNumber);
        verifyNoMoreInteractions(mockRentalRepository);
        verifyZeroInteractions(mockTruckRepository);
    }

    @Test
    public void findAll() {
        Rental mockRental1 = mock(Rental.class);
        Rental mockRental2 = mock(Rental.class);
        List<Rental> toBeReturned = Arrays.asList(mockRental1, mockRental2);
        when(mockRentalRepository.findAll()).thenReturn(toBeReturned);

        Collection<Rental> rentals = rentalService.findAll();
        assertThat(rentals).hasSameElementsAs(toBeReturned);

        verify(mockRentalRepository).findAll();
    }
}
