package io.pivotal.pal.wehaul.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.fleet.domain.DistanceSinceLastInspection;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.service.FleetTruckService;
import io.pivotal.pal.wehaul.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/fleet/trucks")
public class FleetTruckController {

    private final FleetTruckService fleetTruckService;
    private final RentalService rentalService;

    public FleetTruckController(FleetTruckService fleetTruckService, RentalService rentalService) {
        this.fleetTruckService = fleetTruckService;
        this.rentalService = rentalService;
    }

    @GetMapping
    public ResponseEntity<Collection<FleetTruck>> getAllTrucks() {
        Collection<FleetTruck> trucks = fleetTruckService.findAll();
        return ResponseEntity.ok(trucks);
    }

    @PostMapping
    public ResponseEntity<Void> buyTruck(@RequestBody BuyTruckDto buyTruckDto) {

        String vin = buyTruckDto.getVin();
        int odometerReading = buyTruckDto.getOdometerReading();

        FleetTruck fleetTruck = fleetTruckService.buyTruck(vin, odometerReading);
        rentalService.createRentableTruck(
                vin,
                fleetTruck.getMakeModel().getMake(),
                fleetTruck.getMakeModel().getModel()
        );
        rentalService.removeRentableTruck(vin);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{vin}/send-for-inspection")
    public ResponseEntity<Void> sendForInspection(@PathVariable String vin) {

        fleetTruckService.sendForInspection(vin);
        rentalService.removeRentableTruck(vin);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{vin}/return-from-inspection")
    public ResponseEntity<Void> returnFromInspection(
            @PathVariable String vin,
            @RequestBody ReturnFromInspectionDto returnFromInspectionDto
    ) {

        String notes = returnFromInspectionDto.getNotes();
        int odometerReading = returnFromInspectionDto.getOdometerReading();

        fleetTruckService.returnFromInspection(vin, notes, odometerReading);
        rentalService.addRentableTruck(vin);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/distance-since-last-inspections")
    public Collection<DistanceSinceLastInspection> listDistanceSinceLastInspections() {
        return fleetTruckService.findAllDistanceSinceLastInspections();
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
        public BuyTruckDto(@JsonProperty(value = "vin", required = true) String vin,
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
