package io.pivotal.pal.wehaul.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckSinceInspection;
import io.pivotal.pal.wehaul.service.FleetTruckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class FleetTruckController {

    private final FleetTruckService fleetTruckService;

    public FleetTruckController(FleetTruckService fleetTruckService) {
        this.fleetTruckService = fleetTruckService;
    }

    @PostMapping("/trucks")
    public ResponseEntity<Void> buyTruck(@RequestBody BuyTruckDto buyTruckDto) {

        fleetTruckService.buyTruck(
            buyTruckDto.getVin(),
            buyTruckDto.getOdometerReading()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trucks")
    public ResponseEntity<Collection<Truck>> getAllTrucks() {
        Collection<Truck> trucks = fleetTruckService.findAll();
        return ResponseEntity.ok(trucks);
    }

    @PostMapping("/trucks/{vin}/send-for-inspection")
    public ResponseEntity<Void> sendForInspection(@PathVariable String vin) {

        fleetTruckService.sendForInspection(vin);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/trucks/{vin}/return-from-inspection")
    public ResponseEntity<Void> returnFromInspection(
        @PathVariable String vin,
        @RequestBody ReturnFromInspectionDto returnFromInspectionDto
    ) {

        fleetTruckService.returnFromInspection(
            vin,
            returnFromInspectionDto.getNotes(),
            returnFromInspectionDto.getOdometerReading()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/truck-since-inspections")
    public Collection<TruckSinceInspection> listTruckSinceInspections() {
        return fleetTruckService.findAllTruckSinceInspections();
    }

    private static class ReturnFromInspectionDto {

        private final String notes;
        private final int odometerReading;

        @JsonCreator
        private ReturnFromInspectionDto(
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

    private static class BuyTruckDto {

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
