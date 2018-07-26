package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.Rental;
import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import io.pivotal.pal.wehaul.repository.RentalRepository;
import io.pivotal.pal.wehaul.repository.TruckRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RentalServiceRefactorTest {

    @Mock
    private RentalRepository mockRentalRepository;
    @Mock
    private TruckRepository mockTruckRepository;
    @Mock
    private Truck truck;
    @Mock
    private Rental rental;

    private RentalService rentalService;

    @Before
    public void setUp() {
        rentalService = new RentalService(mockRentalRepository, mockTruckRepository);
    }

    @Test
    public void create() {
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);

        rentalService.createRental("some-customer-name");

        verify(truck).reserve();
        verify(truck, times(0)).getStatus();
        verify(truck, times(0)).setStatus(any());
    }

    @Test
    public void pickUp() {
        when(mockRentalRepository.findOne(any())).thenReturn(rental);
        when(rental.getConfirmationNumber()).thenReturn(UUID.randomUUID());
        when(rental.getDistanceTraveled()).thenReturn(null);

        Truck truck = Truck.buyTruck("test-0001", 1);
        truck.setStatus(TruckStatus.RESERVED);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        rentalService.pickUp(UUID.randomUUID());

        verify(rental).pickUp();
        verify(rental, times(0)).getConfirmationNumber();
        verify(rental, times(0)).getDistanceTraveled();
        verify(rental, times(0)).setDistanceTraveled(any());
    }

    @Test
    public void dropOff() {
        when(mockRentalRepository.findOne(any())).thenReturn(rental);
        when(rental.getConfirmationNumber()).thenReturn(UUID.randomUUID());
        when(rental.getDistanceTraveled()).thenReturn(999);

        when(mockTruckRepository.findOne(any())).thenReturn(truck);
        int odometerReading = 10;
        when(truck.getOdometerReading()).thenReturn(odometerReading);
        when(truck.getStatus()).thenReturn(TruckStatus.RESERVED);

        int distanceTraveled = 20;
        rentalService.dropOff(UUID.randomUUID(), distanceTraveled);

        verify(rental).dropOff(distanceTraveled);
        verify(rental, times(0)).setDistanceTraveled(any());
        verify(rental, times(0)).getConfirmationNumber();
        verify(rental, times(0)).getDistanceTraveled();

        verify(truck).returnToService(odometerReading + distanceTraveled);
        verify(truck, times(0)).setStatus(any());
        verify(truck, times(0)).setOdometerReading(anyInt());
        verify(truck, times(0)).getStatus();
        verify(truck, times(1)).getOdometerReading();
    }
}
