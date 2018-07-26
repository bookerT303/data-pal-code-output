package io.pivotal.pal.wehaul.eventlistener;

import io.pivotal.pal.wehaul.rental.domain.event.RentalTruckDroppedOff;
import io.pivotal.pal.wehaul.rental.domain.event.RentalTruckReserved;
import io.pivotal.pal.wehaul.service.FleetService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Async
public class RentalEventListener {

    private final FleetService fleetService;

    public RentalEventListener(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @EventListener
    public void onRentalTruckReserved(RentalTruckReserved event) {
        fleetService.removeTruckFromYard(event.getVin());
    }

    @EventListener
    public void onRentalTruckDroppedOff(RentalTruckDroppedOff event) {
        fleetService.returnTruckToYard(event.getVin(), event.getDistanceTraveled());
    }
}
