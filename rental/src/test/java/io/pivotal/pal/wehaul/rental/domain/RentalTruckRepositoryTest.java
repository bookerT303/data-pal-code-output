package io.pivotal.pal.wehaul.rental.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@DataJpaTest
public class RentalTruckRepositoryTest {

    @Autowired
    private RentalTruckRepository rentalTruckRepository;
    @Mock
    private TruckSizeLookupClient mockTruckInfoLookupClient;

    @Test
    public void findOneByRentalConfirmationNumber() {
        rentalTruckRepository.deleteAll();
        RentalTruck rentalTruck1 = new RentalTruck.Factory(mockTruckInfoLookupClient).createRentableTruck("test-0001", "some-make", "some-model");
        rentalTruck1.reserve("some-customer-name");
        rentalTruckRepository.save(rentalTruck1);

        RentalTruck rentalTruck = rentalTruckRepository.findOneByRentalConfirmationNumber(rentalTruck1.getRental().getConfirmationNumber());

        assertThat(rentalTruck).isNotNull().isEqualToComparingOnlyGivenFields(rentalTruck1, "vin");
    }

    @Test
    public void findTop1ByStatus_noneFound() {
        rentalTruckRepository.deleteAll();

        RentalTruck rentalTruck = rentalTruckRepository.findOneByRentalConfirmationNumber(UUID.randomUUID());

        assertThat(rentalTruck).isNull();
    }

}
