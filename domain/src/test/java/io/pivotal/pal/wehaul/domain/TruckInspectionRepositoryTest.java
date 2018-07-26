package io.pivotal.pal.wehaul.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@DataJpaTest
public class TruckInspectionRepositoryTest {

    @Autowired
    private TruckInspectionRepository truckInspectionRepository;

    @Test
    public void repositoryWires() {
        TruckInspection truckInspection =
                TruckInspection.createTruckInspection("test-0001", 4000, "some-notes");
        truckInspectionRepository.save(truckInspection);

        TruckInspection foundEntry = truckInspectionRepository.findOne(truckInspection.getId());

        assertThat(foundEntry).isEqualToIgnoringGivenFields(truckInspection, "id");
    }

    @SpringBootApplication
    static class TestApp {

        public static void main(String[] args) {
            SpringApplication.run(TestApp.class, args);
        }
    }
}
