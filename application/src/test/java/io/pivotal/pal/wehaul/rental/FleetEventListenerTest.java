package io.pivotal.pal.wehaul.rental;

import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.command.MakeModel;
import io.pivotal.pal.wehaul.fleet.domain.command.TruckInspection;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckReturnedFromInspection;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckSentForInspection;
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

        FleetTruckPurchased event = new FleetTruckPurchased(
                mockFleetTruck.getVin(),
                mockFleetTruck.getMakeModel().getMake(),
                mockFleetTruck.getMakeModel().getModel(),
                mockFleetTruck.getOdometerReading()
        );
        applicationEventPublisher.publishEvent(event);

        verify(mockRentalService, timeout(100)).addTruck(vin, make, model);
    }

    @Test
    public void onFleetTruckSentForInspection() {
        FleetTruck mockFleetTruck = mock(FleetTruck.class);
        String vin = "vin";
        when(mockFleetTruck.getVin()).thenReturn(vin);

        FleetTruckSentForInspection event = new FleetTruckSentForInspection(mockFleetTruck.getVin());
        applicationEventPublisher.publishEvent(event);

        verify(mockRentalService, timeout(100)).preventRenting(vin);
    }

    @Test
    public void onFleetTruckReturnedFromInspection() {
        FleetTruck mockFleetTruck = mock(FleetTruck.class);
        String vin = "vin";
        when(mockFleetTruck.getVin()).thenReturn(vin);

        TruckInspection mockTruckInspection = mock(TruckInspection.class);

        FleetTruckReturnedFromInspection event = new FleetTruckReturnedFromInspection(
                mockFleetTruck.getVin(),
                mockTruckInspection.getOdometerReading(),
                mockTruckInspection.getNotes()
        );
        applicationEventPublisher.publishEvent(event);

        verify(mockRentalService, timeout(100)).allowRenting(vin);
    }
}
