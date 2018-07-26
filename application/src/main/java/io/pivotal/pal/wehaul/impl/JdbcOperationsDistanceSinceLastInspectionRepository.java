package io.pivotal.pal.wehaul.impl;

import io.pivotal.pal.wehaul.fleet.domain.DistanceSinceLastInspection;
import io.pivotal.pal.wehaul.fleet.domain.DistanceSinceLastInspectionRepository;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class JdbcOperationsDistanceSinceLastInspectionRepository implements DistanceSinceLastInspectionRepository {

    private static final String FIND_ALL =
            "SELECT ti.truck_vin, t.odometer_reading - MAX(ti.odometer_reading) " +
                    "FROM truck_inspection ti, fleet_truck t " +
                    "WHERE t.vin = ti.truck_vin " +
                    "GROUP BY ti.truck_vin, t.odometer_reading";

    private final JdbcOperations jdbcOperations;

    public JdbcOperationsDistanceSinceLastInspectionRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public Collection<DistanceSinceLastInspection> findAll() {

        return jdbcOperations.query(
                FIND_ALL,
                (rs, rowNum) -> new DistanceSinceLastInspection(
                        rs.getString(1),
                        rs.getInt(2)
                )
        );
    }
}
