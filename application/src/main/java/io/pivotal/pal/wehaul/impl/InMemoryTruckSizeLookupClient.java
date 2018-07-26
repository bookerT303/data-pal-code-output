package io.pivotal.pal.wehaul.impl;

import io.pivotal.pal.wehaul.domain.MakeModel;
import io.pivotal.pal.wehaul.domain.TruckSize;
import io.pivotal.pal.wehaul.domain.TruckSizeLookupClient;

import java.util.HashMap;
import java.util.Map;

public class InMemoryTruckSizeLookupClient implements TruckSizeLookupClient {

    private final Map<String, TruckSize> dataStore;

    public InMemoryTruckSizeLookupClient() {
        dataStore = new HashMap<>();
        dataStore.put("TruckCo" + "The Big One", TruckSize.LARGE);
        dataStore.put("TruckCo" + "The Small One", TruckSize.SMALL);
    }

    @Override
    public TruckSize getSizeByMakeModel(MakeModel makeModel) {
        return dataStore.get(makeModel.getMake() + makeModel.getModel());
    }
}
