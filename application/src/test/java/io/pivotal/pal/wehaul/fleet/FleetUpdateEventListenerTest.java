package io.pivotal.pal.wehaul.fleet;

import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruckStatus;
import io.pivotal.pal.wehaul.fleet.domain.command.MakeModel;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckQueryRepository;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckSnapshot;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckUpdatedEvent;
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
public class FleetUpdateEventListenerTest {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private FleetTruckQueryRepository fleetTruckQueryRepository;

    @Test
    public void onFleetTruckUpdated() {
        FleetTruck fleetTruck = mock(FleetTruck.class);
        when(fleetTruck.getVin()).thenReturn("vin");
        when(fleetTruck.getStatus()).thenReturn(FleetTruckStatus.INSPECTABLE);
        when(fleetTruck.getOdometerReading()).thenReturn(1000);
        when(fleetTruck.getMakeModel()).thenReturn(new MakeModel("make", "model"));

        FleetTruckUpdatedEvent event = new FleetTruckUpdatedEvent(fleetTruck);

        applicationEventPublisher.publishEvent(event);

        verify(fleetTruckQueryRepository, timeout(100)).save(new FleetTruckSnapshot("vin", "INSPECTABLE", 1000, "make", "model"));
    }
}