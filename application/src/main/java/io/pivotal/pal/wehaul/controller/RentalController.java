package io.pivotal.pal.wehaul.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.service.FleetService;
import io.pivotal.pal.wehaul.rental.domain.Rental;
import io.pivotal.pal.wehaul.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
public class RentalController {

    private final RentalService rentalService;
    private final FleetService fleetService;

    public RentalController(RentalService rentalService, FleetService fleetService) {
        this.rentalService = rentalService;
        this.fleetService = fleetService;
    }

    @PostMapping("/rentals")
    public ResponseEntity<Void> createRental(@RequestBody CreateRentalDto createRentalDto) {

        String customerName = createRentalDto.getCustomerName();
        Rental rental = rentalService.createRental(customerName);

        String truckVin = rental.getTruckVin();
        fleetService.removeFromYard(truckVin);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/rentals/{rentalId}/pick-up")
    public ResponseEntity<Void> pickUpRental(@PathVariable UUID rentalId) {

        rentalService.pickUp(rentalId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rentals/{rentalId}/drop-off")
    public ResponseEntity<Void> dropOffRental(@PathVariable UUID rentalId,
                                              @RequestBody DropOffRentalDto dropOffRentalDto) {

        int distanceTraveled = dropOffRentalDto.getDistanceTraveled();
        Rental rental = rentalService.dropOff(rentalId, distanceTraveled);
        String vin = rental.getTruckVin();
        fleetService.returnToYard(vin, distanceTraveled);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/rentals")
    public ResponseEntity<Collection<Rental>> getAllRentals() {
        Collection<Rental> rentals = rentalService.findAll();
        return ResponseEntity.ok(rentals);
    }

    private static class DropOffRentalDto {

        private final int distanceTraveled;

        @JsonCreator
        DropOffRentalDto(@JsonProperty(value = "distanceTraveled", required = true) int distanceTraveled) {
            this.distanceTraveled = distanceTraveled;
        }

        int getDistanceTraveled() {
            return distanceTraveled;
        }

        @Override
        public String toString() {
            return "DropOffRentalDto{" +
                    "distanceTraveled=" + distanceTraveled +
                    '}';
        }
    }

    private static class CreateRentalDto {

        private final String customerName;

        @JsonCreator
        CreateRentalDto(@JsonProperty(value = "customerName", required = true) String customerName) {
            this.customerName = customerName;
        }

        String getCustomerName() {
            return customerName;
        }

        @Override
        public String toString() {
            return "CreateRentalDto{" +
                    "customerName='" + customerName + '\'' +
                    '}';
        }
    }
}
