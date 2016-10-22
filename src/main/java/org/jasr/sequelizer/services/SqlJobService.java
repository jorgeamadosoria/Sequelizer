package org.jasr.sequelizer.services;

import java.util.Map;

import org.jasr.sequelizer.entities.ExecutionStatus;

public interface SqlJobService {
    public ExecutionStatus execute(long id, Map<String, Object> parameters, boolean csvExport);
    public ExecutionStatus count(long id, Map<String, Object> parameters, boolean csvExport);
}
