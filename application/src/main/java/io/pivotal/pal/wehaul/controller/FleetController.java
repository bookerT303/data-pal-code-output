package io.pivotal.pal.wehaul.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.domain.DistanceSinceLastInspection;
import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.service.FleetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class FleetController {

    private final FleetService fleetService;

    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @PostMapping("/trucks")
    public ResponseEntity<Void> buyTruck(@RequestBody BuyTruckDto buyTruckDto) {

        fleetService.buyTruck(
                buyTruckDto.getVin(),
                buyTruckDto.getOdometerReading()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trucks")
    public ResponseEntity<Collection<Truck>> getAllTrucks() {
        Collection<Truck> trucks = fleetService.findAll();
        return ResponseEntity.ok(trucks);
    }

    @PostMapping("/trucks/{vin}/send-for-inspection")
    public ResponseEntity<Void> sendForInspection(@PathVariable String vin) {

        fleetService.sendForInspection(vin);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/trucks/{vin}/return-from-inspection")
    public ResponseEntity<Void> returnFromInspection(
            @PathVariable String vin,
            @RequestBody ReturnFromInspectionDto returnFromInspectionDto
    ) {

        String notes = returnFromInspectionDto.getNotes();
        int odometerReading = returnFromInspectionDto.getOdometerReading();

        fleetService.returnFromInspection(vin, notes, odometerReading);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/truck-since-inspections")
    public Collection<DistanceSinceLastInspection> listDistanceSinceLastInspections() {
        return fleetService.findAllDistanceSinceLastInspections();
    }

    private static class ReturnFromInspectionDto {

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

        String getNotes() {
            return notes;
        }

        int getOdometerReading() {
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

    private static class BuyTruckDto {

        private final String vin;
        private final int odometerReading;

        @JsonCreator
        BuyTruckDto(@JsonProperty(value = "vin", required = true) String vin,
                           @JsonProperty(value = "odometerReading", required = true) int odometerReading) {
            this.vin = vin;
            this.odometerReading = odometerReading;
        }

        String getVin() {
            return vin;
        }

        int getOdometerReading() {
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
