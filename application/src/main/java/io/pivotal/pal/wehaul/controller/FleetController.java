package io.pivotal.pal.wehaul.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.service.FleetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/fleet/trucks")
public class FleetController {

    private final FleetService fleetService;

    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @GetMapping
    public ResponseEntity<Collection<FleetTruck>> getAllTrucks() {
        Collection<FleetTruck> trucks = fleetService.findAll();
        return ResponseEntity.ok(trucks);
    }

    @GetMapping("/{truckVin}")
    public ResponseEntity<FleetTruck> getTruck(@PathVariable String truckVin) {
        FleetTruck truck = fleetService.findOne(truckVin);
        return ResponseEntity.ok(truck);
    }

    @PostMapping
    public ResponseEntity<Void> buyTruck(@RequestBody BuyTruckDto buyTruckDto) {

        String vin = buyTruckDto.getVin();
        int odometerReading = buyTruckDto.getOdometerReading();

        fleetService.buyTruck(vin, odometerReading);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{vin}/send-for-inspection")
    public ResponseEntity<Void> sendTruckForInspection(@PathVariable String vin) {

        fleetService.sendTruckForInspection(vin);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{vin}/return-from-inspection")
    public ResponseEntity<Void> returnTruckFromInspection(
            @PathVariable String vin,
            @RequestBody ReturnTruckFromInspectionDto returnTruckFromInspectionDto
    ) {

        String notes = returnTruckFromInspectionDto.getNotes();
        int odometerReading = returnTruckFromInspectionDto.getOdometerReading();

        fleetService.returnTruckFromInspection(vin, notes, odometerReading);

        return ResponseEntity.ok().build();
    }

    static class ReturnTruckFromInspectionDto {

        private final String notes;
        private final int odometerReading;

        @JsonCreator
        ReturnTruckFromInspectionDto(
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
            return "ReturnTruckFromInspectionDto{" +
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
