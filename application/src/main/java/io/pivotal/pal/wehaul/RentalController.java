package io.pivotal.pal.wehaul;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.fleet.service.FleetTruckService;
import io.pivotal.pal.wehaul.rental.domain.Rental;
import io.pivotal.pal.wehaul.rental.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
public class RentalController {

    private final RentalService rentalService;
    private final FleetTruckService fleetTruckService;

    public RentalController(RentalService rentalService, FleetTruckService fleetTruckService) {
        this.rentalService = rentalService;
        this.fleetTruckService = fleetTruckService;
    }

    @PostMapping("/rentals")
    public ResponseEntity<Void> createRental(@RequestBody CreateRentalDto createRentalDto) {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    @PostMapping("/rentals/{rentalId}/pick-up")
    public ResponseEntity<Void> pickUpRental(@PathVariable UUID rentalId) {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    @PostMapping("/rentals/{rentalId}/drop-off")
    public ResponseEntity<Void> dropOffRental(@PathVariable UUID rentalId,
                                              @RequestBody DropOffRentalDto dropOffRentalDto) {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    @GetMapping("/rentals")
    public ResponseEntity<Collection<Rental>> getAllRentals() {
        Collection<Rental> rentals = rentalService.findAll();
        return ResponseEntity.ok(rentals);
    }

    static class DropOffRentalDto {

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

    static class CreateRentalDto {

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
