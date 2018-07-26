package io.pivotal.pal.wehaul.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.rental.domain.Rental;
import io.pivotal.pal.wehaul.rental.domain.RentalTruck;
import io.pivotal.pal.wehaul.service.FleetTruckService;
import io.pivotal.pal.wehaul.service.RentalService;
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
    private final FleetTruckService fleetTruckService;

    public RentalController(RentalService rentalService, FleetTruckService fleetTruckService) {
        this.rentalService = rentalService;
        this.fleetTruckService = fleetTruckService;
    }

    @GetMapping
    public ResponseEntity<Collection<Rental>> getAllRentals() {
        Collection<Rental> rentals = rentalService.findAll();
        return ResponseEntity.ok(rentals);
    }

    @PostMapping
    public ResponseEntity<Void> createRental(@RequestBody CreateRentalDto createRentalDto) {

        String customerName = createRentalDto.getCustomerName();
        RentalTruck rentalTruck = rentalService.createRental(customerName);

        String vin = rentalTruck.getVin();
        fleetTruckService.removeFromYard(vin);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{confirmationNumber}/pick-up")
    public ResponseEntity<Void> pickUpRental(@PathVariable UUID confirmationNumber) {

        rentalService.pickUp(confirmationNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{confirmationNumber}/drop-off")
    public ResponseEntity<Void> dropOffRental(@PathVariable UUID confirmationNumber,
                                              @RequestBody DropOffRentalDto dropOffRentalDto) {

        int distanceTraveled = dropOffRentalDto.getDistanceTraveled();
        RentalTruck rental = rentalService.dropOff(confirmationNumber);
        String vin = rental.getVin();
        fleetTruckService.returnToYard(vin, distanceTraveled);

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
