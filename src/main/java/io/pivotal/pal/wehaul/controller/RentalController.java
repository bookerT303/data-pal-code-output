package io.pivotal.pal.wehaul.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.domain.Rental;
import io.pivotal.pal.wehaul.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping("/rentals")
    public ResponseEntity<Void> createRental(@RequestBody CreateRentalDto createRentalDto) {

        String customerName = createRentalDto.getCustomerName();
        rentalService.createRental(customerName);

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
        rentalService.dropOff(rentalId, distanceTraveled);

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
        public DropOffRentalDto(@JsonProperty(value = "distanceTraveled", required = true) int distanceTraveled) {
            this.distanceTraveled = distanceTraveled;
        }

        public int getDistanceTraveled() {
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
        public CreateRentalDto(@JsonProperty(value = "customerName", required = true) String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerName() {
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
