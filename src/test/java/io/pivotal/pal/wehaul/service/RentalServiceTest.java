package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.Rental;
import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import io.pivotal.pal.wehaul.repository.RentalRepository;
import io.pivotal.pal.wehaul.repository.TruckRepository;
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
        Truck truck = new Truck("test-0001", 0);
        truck.setStatus(TruckStatus.RENTABLE);
        truck.setOdometerReading(1);
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);

        Rental rental = new Rental("some-customer-name", truck.getVin());
        when(mockRentalRepository.findOne(any())).thenReturn(rental);

        // method under test
        rentalService.createRental(rental.getCustomerName());

        // Verify the truck and rental repo were used
        verify(mockTruckRepository).findTop1ByStatus(TruckStatus.RENTABLE);
        verify(mockTruckRepository).save(truckCaptor.capture());
        verify(mockRentalRepository).save(rentalCaptor.capture());

        // Truck should now be reserved
        Truck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck.getStatus()).isEqualTo(TruckStatus.RESERVED);

        // Rental should now have confirmation number, and a ref to the truck by id
        Rental savedRental = rentalCaptor.getValue();
        assertThat(savedRental.getConfirmationNumber()).isNotNull();
        assertThat(savedRental.getTruckVin()).isEqualTo(truck.getVin());
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
        // set up confirmed rental
        Truck truck = new Truck("test-0001", 1000);
        truck.setStatus(TruckStatus.RENTABLE);
        truck.setOdometerReading(2000);
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        Rental rental = new Rental("some-customer-name", truck.getVin());
        when(mockRentalRepository.findOne(any())).thenReturn(rental);

        rentalService.createRental(rental.getCustomerName());

        // method under test
        rentalService.pickUp(rental.getConfirmationNumber());

        // assertions + verifications
        assertThat(rental.getDistanceTraveled()).isEqualTo(0);

        verify(mockRentalRepository).findOne(rental.getConfirmationNumber());
        verify(mockRentalRepository).save(rental);

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.RENTED);

        verify(mockTruckRepository).findOne(truck.getVin());
        verify(mockTruckRepository, times(2)).save(truck);
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
        // set up rental to picked up state
        Truck truck = new Truck("test-0001", 0);
        truck.setStatus(TruckStatus.RENTABLE);
        truck.setOdometerReading(1);
        when(mockTruckRepository.findTop1ByStatus(any())).thenReturn(truck);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        Rental rental = new Rental("some-customer-name", truck.getVin());
        when(mockRentalRepository.findOne(any())).thenReturn(rental);

        rentalService.createRental("some-customer-name");
        rentalService.pickUp(rental.getConfirmationNumber());

        // method under test
        int distanceTraveled = 1000000;
        rentalService.dropOff(rental.getConfirmationNumber(), distanceTraveled);

        // assertions + verifications
        assertThat(rental.getDistanceTraveled()).isEqualTo(distanceTraveled);

        verify(mockTruckRepository, times(3)).save(truckCaptor.capture());
        verify(mockRentalRepository, times(3)).save(rentalCaptor.capture());

        // assert on truck is Rentable
        Truck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck.getStatus()).isEqualTo(TruckStatus.RENTABLE);
        assertThat(savedTruck.getOdometerReading()).isEqualTo(1000000 + 1);
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
}
