package io.pivotal.pal.wehaul.rental;

import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckReturnedFromInspection;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckSentForInspection;
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
        rentalService.preventRenting(event.getVin());
    }

    @EventListener
    public void onFleetTruckReturnedFromInspection(FleetTruckReturnedFromInspection event) {
        rentalService.allowRenting(event.getVin());
    }
}
