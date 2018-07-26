package io.pivotal.pal.wehaul.fleet.domain.command;

public interface TruckInfoLookupClient {

    MakeModel getMakeModelByVin(String vin);
}
