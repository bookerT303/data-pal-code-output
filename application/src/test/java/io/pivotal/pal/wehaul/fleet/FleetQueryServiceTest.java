package io.pivotal.pal.wehaul.fleet;

import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckQueryRepository;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckSnapshot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FleetQueryServiceTest {

    @Mock
    private FleetTruckQueryRepository fleetTruckQueryRepository;

    private FleetQueryService fleetQueryService;

    @Before
    public void setUp() {
        fleetQueryService = new FleetQueryService(fleetTruckQueryRepository);
    }

    @Test
    public void findAll() {
        FleetTruckSnapshot mockTruck1 = mock(FleetTruckSnapshot.class);
        FleetTruckSnapshot mockTruck2 = mock(FleetTruckSnapshot.class);
        List<FleetTruckSnapshot> toBeReturned = Arrays.asList(mockTruck1, mockTruck2);
        when(fleetTruckQueryRepository.findAll()).thenReturn(toBeReturned);

        Collection<FleetTruckSnapshot> trucks = fleetQueryService.findAll();
        assertThat(trucks).hasSameElementsAs(toBeReturned);

        verify(fleetTruckQueryRepository).findAll();
    }
}