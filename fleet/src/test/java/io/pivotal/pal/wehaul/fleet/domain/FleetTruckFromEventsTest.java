package io.pivotal.pal.wehaul.fleet.domain;

import io.pivotal.pal.wehaul.fleet.domain.event.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FleetTruckFromEventsTest {

    @Test
    public void purchaseTruck_fromEvents() {
        List<FleetTruckEvent> events = Collections.singletonList(
                new FleetTruckPurchased("some-vin", "MAKE", "MODEL", 100)
        );

        FleetTruck truck = new FleetTruck(events);

        assertThat(truck.getVin()).isEqualTo("some-vin");
        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
        assertThat(truck.getOdometerReading()).isEqualTo(100);
        assertThat(truck.getMakeModel()).isEqualTo(new MakeModel("MAKE", "MODEL"));

        assertThat(truck.fleetDomainEvents()).hasSize(0);
    }

    @Test
    public void returnFromInspection_fromEvents() {
        List<FleetTruckEvent> events = Arrays.asList(
                new FleetTruckPurchased("some-vin", "MAKE", "MODEL", 100),
                new FleetTruckReturnedFromInspection("some-vin", 100, "NOTES")
        );

        FleetTruck truck = new FleetTruck(events);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(100);

        assertThat(truck.getInspections()).hasSize(1);
        assertThat(truck.getInspections().get(0)).isEqualToIgnoringGivenFields(
                TruckInspection.createTruckInspection("some-vin", 100, "NOTES"),
                "id"
        );

        assertThat(truck.fleetDomainEvents()).hasSize(0);
    }

    @Test
    public void sendForInspection_fromEvents() {
        List<FleetTruckEvent> events = Arrays.asList(
                new FleetTruckPurchased("some-vin", "MAKE", "MODEL", 100),
                new FleetTruckReturnedFromInspection("some-vin", 100, "NOTES"),
                new FleetTruckSentForInspection("some-vin")
        );

        FleetTruck truck = new FleetTruck(events);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);

        assertThat(truck.fleetDomainEvents()).hasSize(0);
    }

    @Test
    public void removeFromYard_fromEvents() {
        List<FleetTruckEvent> events = Arrays.asList(
                new FleetTruckPurchased("some-vin", "MAKE", "MODEL", 100),
                new FleetTruckReturnedFromInspection("some-vin", 100, "NOTES"),
                new FleetTruckRemovedFromYard("some-vin")
        );

        FleetTruck truck = new FleetTruck(events);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.NOT_INSPECTABLE);

        assertThat(truck.fleetDomainEvents()).hasSize(0);
    }

    @Test
    public void returnToYard_fromEvents() {
        List<FleetTruckEvent> events = Arrays.asList(
                new FleetTruckPurchased("some-vin", "MAKE", "MODEL", 100),
                new FleetTruckReturnedFromInspection("some-vin", 100, "NOTES"),
                new FleetTruckRemovedFromYard("some-vin"),
                new FleetTruckReturnedToYard("some-vin", 200)
        );

        FleetTruck truck = new FleetTruck(events);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(300);

        assertThat(truck.fleetDomainEvents()).hasSize(0);
    }
}
