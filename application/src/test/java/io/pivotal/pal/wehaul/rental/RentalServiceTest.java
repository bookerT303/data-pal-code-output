package io.pivotal.pal.wehaul.rental;

import io.pivotal.pal.wehaul.rental.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
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
    private TruckSizeLookupClient mockTruckSizeLookupClient;
    @Mock
    private RentalTruckRepository mockTruckRepository;
    @Captor
    private ArgumentCaptor<RentalTruck> truckCaptor;

    private RentalService rentalService;

    @Before
    public void setUp() {
        rentalService = new RentalService(mockTruckSizeLookupClient, mockTruckRepository);
    }

    @Test
    public void reserve() {
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn("some-vin");
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(mockTruck);

        rentalService.reserve("some-customer-name");

        InOrder inOrder = inOrder(mockTruck, mockTruckRepository);
        inOrder.verify(mockTruckRepository).findTop1ByStatus(RentalTruckStatus.RENTABLE);
        inOrder.verify(mockTruck).reserve("some-customer-name");
        inOrder.verify(mockTruckRepository).save(truckCaptor.capture());

        RentalTruck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck).isSameAs(mockTruck);
    }

    @Test
    public void reserve_whenNoTruckAvailable() {
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> rentalService.reserve("some-customer-name"))
                .withMessage("No trucks available to rent");

        verify(mockTruckRepository).findTop1ByStatus(RentalTruckStatus.RENTABLE);
        verifyNoMoreInteractions(mockTruckRepository);
    }

    @Test
    public void pickUp() {
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruckRepository.findOneByRentalConfirmationNumber(any())).thenReturn(mockTruck);

        UUID confirmationNumber = UUID.randomUUID();
        rentalService.pickUp(confirmationNumber);

        InOrder inOrder = inOrder(mockTruck, mockTruckRepository);
        inOrder.verify(mockTruckRepository).findOneByRentalConfirmationNumber(confirmationNumber);
        inOrder.verify(mockTruck).pickUp();
        inOrder.verify(mockTruckRepository).save(mockTruck);
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
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruckRepository.findOneByRentalConfirmationNumber(any())).thenReturn(mockTruck);

        UUID confirmationNumber = UUID.randomUUID();
        int distanceTraveled = 500;
        rentalService.dropOff(confirmationNumber, distanceTraveled);

        InOrder inOrder = inOrder(mockTruck, mockTruckRepository);
        inOrder.verify(mockTruckRepository).findOneByRentalConfirmationNumber(confirmationNumber);
        inOrder.verify(mockTruck).dropOff(distanceTraveled);
        inOrder.verify(mockTruckRepository).save(truckCaptor.capture());
        RentalTruck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck).isSameAs(mockTruck);
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
        RentalTruckSize truckSize = RentalTruckSize.LARGE;
        when(mockTruckSizeLookupClient.getSizeByMakeModel(any(), any())).thenReturn(truckSize);

        String vin = "cool-vin";
        String make = "make";
        String model = "model";
        rentalService.addTruck(vin, make, model);

        InOrder inOrder = inOrder(mockTruckSizeLookupClient, mockTruckRepository);
        inOrder.verify(mockTruckSizeLookupClient).getSizeByMakeModel(make, model);
        inOrder.verify(mockTruckRepository).save(truckCaptor.capture());

        RentalTruck rentalTruck = truckCaptor.getValue();
        assertThat(rentalTruck.getVin()).isEqualTo(vin);
        assertThat(rentalTruck.getStatus()).isEqualTo(RentalTruckStatus.NOT_RENTABLE);
        assertThat(rentalTruck.getSize()).isEqualTo(truckSize);
        assertThat(rentalTruck.getRental()).isNull();
    }

    @Test
    public void preventRenting() {
        String vin = "best-vin";
        RentalTruck mockTruck = mock(RentalTruck.class);
        when(mockTruck.getVin()).thenReturn(vin);
        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);

        rentalService.preventRenting(vin);

        InOrder inOrder = inOrder(mockTruckRepository, mockTruck);
        inOrder.verify(mockTruckRepository).findOne(vin);
        inOrder.verify(mockTruck).preventRenting();
        inOrder.verify(mockTruckRepository).save(mockTruck);
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

        InOrder inOrder = inOrder(mockTruckRepository, mockTruck);
        inOrder.verify(mockTruckRepository).findOne("best-vin");
        inOrder.verify(mockTruck).allowRenting();
        inOrder.verify(mockTruckRepository).save(mockTruck);
    }

    @Test
    public void allowRenting_whenTruckNotFound() {
        when(mockTruckRepository.findOne(any())).thenReturn(null);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> rentalService.allowRenting("vin"))
                .withMessage("No truck found with vin=vin");
    }
}
