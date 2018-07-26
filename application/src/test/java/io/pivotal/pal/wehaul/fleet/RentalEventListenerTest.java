package io.pivotal.pal.wehaul.fleet;

import io.pivotal.pal.wehaul.rental.domain.RentalTruck;
import io.pivotal.pal.wehaul.rental.domain.event.RentalTruckDroppedOff;
import io.pivotal.pal.wehaul.rental.domain.event.RentalTruckReserved;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RentalEventListenerTest {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private FleetCommandService mockFleetCommandService;

    @Test
    public void onRentalTruckReserved() {
        RentalTruck mockRentalTruck = mock(RentalTruck.class);
        String vin = "vin";
        when(mockRentalTruck.getVin()).thenReturn(vin);

        applicationEventPublisher.publishEvent(new RentalTruckReserved(mockRentalTruck));

        verify(mockFleetCommandService, timeout(100)).removeTruckFromYard(vin);
    }

    @Test
    public void onRentalTruckDroppedOff() {
        RentalTruck mockRentalTruck = mock(RentalTruck.class);
        String vin = "vin";
        when(mockRentalTruck.getVin()).thenReturn(vin);

        int distanceTraveled = 100;
        applicationEventPublisher.publishEvent(new RentalTruckDroppedOff(mockRentalTruck, distanceTraveled));

        verify(mockFleetCommandService, timeout(100)).returnTruckToYard(vin, distanceTraveled);
    }
}
