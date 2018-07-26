package io.pivotal.pal.wehaul.eventlistener;

import io.pivotal.pal.wehaul.fleet.domain.*;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckReturnedFromInspection;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckSentForInspection;
import io.pivotal.pal.wehaul.service.RentalService;
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
public class FleetEventListenerTest {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private RentalService mockRentalService;

    @Test
    public void onFleetTruckPurchased() {
        FleetTruck mockFleetTruck = mock(FleetTruck.class);
        String vin = "vin";
        String make = "make";
        String model = "model";
        when(mockFleetTruck.getVin()).thenReturn(vin);
        when(mockFleetTruck.getMakeModel()).thenReturn(new MakeModel(make, model));

        applicationEventPublisher.publishEvent(new FleetTruckPurchased(mockFleetTruck));

        verify(mockRentalService, timeout(100)).addTruck(vin, make, model);
    }

    @Test
    public void onFleetTruckSentForInspection() {
        FleetTruck mockFleetTruck = mock(FleetTruck.class);
        String vin = "vin";
        when(mockFleetTruck.getVin()).thenReturn(vin);

        applicationEventPublisher.publishEvent(new FleetTruckSentForInspection(mockFleetTruck));

        verify(mockRentalService, timeout(100)).preventRenting(vin);
    }

    @Test
    public void onFleetTruckReturnedFromInspection() {
        FleetTruck mockFleetTruck = mock(FleetTruck.class);
        String vin = "vin";
        when(mockFleetTruck.getVin()).thenReturn(vin);

        applicationEventPublisher.publishEvent(new FleetTruckReturnedFromInspection(mockFleetTruck));

        verify(mockRentalService, timeout(100)).allowRenting(vin);
    }
}
