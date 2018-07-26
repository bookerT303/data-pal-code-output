package io.pivotal.pal.wehaul.adapter;

import io.pivotal.pal.wehaul.rental.domain.RentalTruckSize;
import io.pivotal.pal.wehaul.rental.domain.TruckSizeLookupClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryTruckSizeLookupClient implements TruckSizeLookupClient {

    private final Map<String, RentalTruckSize> dataStore;

    public InMemoryTruckSizeLookupClient() {
        dataStore = new HashMap<>();
        dataStore.put("TruckCo" + "The Big One", RentalTruckSize.LARGE);
        dataStore.put("TruckCo" + "The Small One", RentalTruckSize.SMALL);
    }

    @Override
    public RentalTruckSize getSizeByMakeModel(String make, String model) {
        return dataStore.get(make + model);
    }

}
