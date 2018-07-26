package io.pivotal.pal.wehaul.fleet.domain.command;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TruckInspectionTest {

    @Test
    public void createTruckInspection() {
        String truckVin = "test-0001";
        int odometerReading = 2200;
        String notes = "some-notes";
        TruckInspection journalEntry =
                TruckInspection.createTruckInspection(truckVin, odometerReading, notes);

        assertThat(journalEntry.getId()).isNotNull();
        assertThat(journalEntry.getTruckVin()).isEqualTo(truckVin);
        assertThat(journalEntry.getOdometerReading()).isEqualTo(odometerReading);
        assertThat(journalEntry.getNotes()).isEqualTo(notes);
    }
}
