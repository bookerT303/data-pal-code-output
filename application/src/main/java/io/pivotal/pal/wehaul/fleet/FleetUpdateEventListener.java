package io.pivotal.pal.wehaul.fleet;

import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckQueryRepository;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckSnapshot;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Async
public class FleetUpdateEventListener {

    private final FleetTruckQueryRepository fleetTruckQueryRepository;

    public FleetUpdateEventListener(FleetTruckQueryRepository fleetTruckQueryRepository) {
        this.fleetTruckQueryRepository = fleetTruckQueryRepository;
    }

    @EventListener
    public void onFleetTruckUpdated(FleetTruckUpdatedEvent event) {
        FleetTruckSnapshot fleetTruckSnapshot =
                new FleetTruckSnapshot(event.getVin(), event.getStatus(), event.getOdometerReading(), event.getMake(), event.getModel());

        fleetTruckQueryRepository.save(fleetTruckSnapshot);
    }
}
