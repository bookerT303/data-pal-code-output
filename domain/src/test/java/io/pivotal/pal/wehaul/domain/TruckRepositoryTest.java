package io.pivotal.pal.wehaul.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@DataJpaTest
public class TruckRepositoryTest {

    @Autowired
    private Truck.Factory truckFactory;

    @Autowired
    private TruckRepository truckRepository;

    @Test
    public void findTop1ByStatus() {
        Truck truck1 = truckFactory.buyTruck("test-0001", 1000);
        Truck truck2 = truckFactory.buyTruck("test-0002", 2000);
        Truck truck3 = truckFactory.buyTruck("test-0003", 3000);
        truck3.returnFromInspection(4000);
        truckRepository.save(Arrays.asList(truck1, truck2, truck3));

        Truck truck = truckRepository.findTop1ByStatus(TruckStatus.RENTABLE);

        assertThat(truck)
                .isNotNull()
                .isEqualToComparingOnlyGivenFields(truck3, "vin");
    }

    @Test
    public void findTop1ByStatus_noneFound() {
        Truck truck1 = truckFactory.buyTruck("test-0001", 1000);
        truckRepository.save(truck1);

        Truck truck = truckRepository.findTop1ByStatus(TruckStatus.RENTABLE);

        assertThat(truck).isNull();
    }

    @SpringBootApplication
    static class TestApp {

        public static void main(String[] args) {
            SpringApplication.run(TruckInspectionRepositoryTest.TestApp.class, args);
        }

        @TestConfiguration
        static class TestConfig {

            @Bean
            public Truck.Factory truckFactory() {
                return new Truck.Factory(
                        vin -> new MakeModel("stubbed-make", "stubbed-model"),
                        makeModel -> TruckSize.LARGE
                );
            }
        }
    }
}
