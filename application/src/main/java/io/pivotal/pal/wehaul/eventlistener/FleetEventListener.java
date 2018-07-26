package io.pivotal.pal.wehaul.eventlistener;

import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckReturnedFromInspection;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckSentForInspection;
import io.pivotal.pal.wehaul.service.RentalService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Async
public class FleetEventListener {

    private final RentalService rentalService;

    public FleetEventListener(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @EventListener
    public void onFleetTruckPurchased(FleetTruckPurchased event) {
        rentalService.addTruck(event.getVin(), event.getMake(), event.getModel());
    }

    @EventListener
    public void onFleetTruckSentForInspection(FleetTruckSentForInspection event) {

    }

    @EventListener
    public void onFleetTruckReturnedFromInspection(FleetTruckReturnedFromInspection event) {

    }
}
