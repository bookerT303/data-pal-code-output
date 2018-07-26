package io.pivotal.pal.wehaul.rental;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.rental.domain.Rental;
import io.pivotal.pal.wehaul.rental.domain.RentalTruck;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public ResponseEntity<Collection<Rental>> getAllRentals() {
        Collection<Rental> rentals = rentalService.findAll().stream()
                .map(RentalTruck::getRental)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rentals);
    }

    @PostMapping
    public ResponseEntity<Void> reserveTruck(@RequestBody ReserveTruckDto reserveTruckDto) {

        String customerName = reserveTruckDto.getCustomerName();
        rentalService.reserve(customerName);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{confirmationNumber}/pick-up")
    public ResponseEntity<Void> pickUp(@PathVariable UUID confirmationNumber) {

        rentalService.pickUp(confirmationNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{confirmationNumber}/drop-off")
    public ResponseEntity<Void> dropOff(@PathVariable UUID confirmationNumber,
                                        @RequestBody DropOffRentalDto dropOffRentalDto) {

        int distanceTraveled = dropOffRentalDto.getDistanceTraveled();
        rentalService.dropOff(confirmationNumber, distanceTraveled);

        return ResponseEntity.ok().build();
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

    static class ReserveTruckDto {

        private final String customerName;

        @JsonCreator
        public ReserveTruckDto(@JsonProperty(value = "customerName", required = true) String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerName() {
            return customerName;
        }

        @Override
        public String toString() {
            return "ReserveTruckDto{" +
                    "customerName='" + customerName + '\'' +
                    '}';
        }
    }
}
