package io.pivotal.pal.wehaul.impl;


import io.pivotal.pal.wehaul.domain.MakeModel;
import io.pivotal.pal.wehaul.domain.TruckInfoLookupClient;

import java.util.HashMap;
import java.util.Map;

public class InMemoryTruckInfoLookupClient implements TruckInfoLookupClient {

    private final Map<String, MakeModel> dataStore;

    public InMemoryTruckInfoLookupClient() {
        dataStore = new HashMap<>();
        dataStore.put("test-0001", new MakeModel("TruckCo", "The Big One"));
        dataStore.put("test-0002", new MakeModel("TruckCo", "The Small One"));
    }

    @Override
    public MakeModel getMakeModelByVin(String vin) {
        return dataStore.getOrDefault(vin, new MakeModel("TruckCo", "The Small One"));
    }
}
