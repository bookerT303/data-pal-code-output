package io.pivotal.pal.wehaul.domain;

public interface TruckSizeLookupClient {
    TruckSize getSizeByMakeModel(MakeModel makeModel);
}
