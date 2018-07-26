package io.pivotal.pal.wehaul;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.fleet.domain.TruckSinceInspection;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.service.FleetTruckService;
import io.pivotal.pal.wehaul.rental.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class FleetTruckController {

    private final FleetTruckService fleetTruckService;
    private final RentalService rentalService;

    public FleetTruckController(FleetTruckService fleetTruckService, RentalService rentalService) {
        this.fleetTruckService = fleetTruckService;
        this.rentalService = rentalService;
    }

    @PostMapping("/trucks")
    public ResponseEntity<Void> buyTruck(@RequestBody BuyTruckDto body) {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    @GetMapping("/trucks")
    public ResponseEntity<Collection<FleetTruck>> getAllTrucks() {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    @PostMapping("/trucks/{vin}/send-for-inspection")
    public ResponseEntity<Void> sendForInspection(@PathVariable String vin) {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    @PostMapping("/trucks/{vin}/return-from-inspection")
    public ResponseEntity<Void> returnFromInspection(
            @PathVariable String vin,
            @RequestBody ReturnFromInspectionDto returnFromInspectionDto
    ) {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    @GetMapping("/truck-since-inspections")
    public Collection<TruckSinceInspection> listTruckSinceInspections() {
        return fleetTruckService.findAllTruckSinceInspections();
    }

    static class ReturnFromInspectionDto {

        private final String notes;
        private final int odometerReading;

        @JsonCreator
        ReturnFromInspectionDto(
                @JsonProperty(value = "notes", required = true) String notes,
                @JsonProperty(value = "odometerReading", required = true) int odometerReading
        ) {
            this.notes = notes;
            this.odometerReading = odometerReading;
        }

        public String getNotes() {
            return notes;
        }

        public int getOdometerReading() {
            return odometerReading;
        }

        @Override
        public String toString() {
            return "ReturnFromInspectionDto{" +
                    "notes='" + notes + '\'' +
                    ", odometerReading=" + odometerReading +
                    '}';
        }
    }

    static class BuyTruckDto {

        private final String vin;
        private final int odometerReading;

        @JsonCreator
        BuyTruckDto(@JsonProperty(value = "vin", required = true) String vin,
                    @JsonProperty(value = "odometerReading", required = true) int odometerReading) {
            this.vin = vin;
            this.odometerReading = odometerReading;
        }

        public String getVin() {
            return vin;
        }

        public int getOdometerReading() {
            return odometerReading;
        }

        @Override
        public String toString() {
            return "BuyTruckDto{" +
                    "vin='" + vin + '\'' +
                    ", odometerReading=" + odometerReading +
                    '}';
        }

    }
}
