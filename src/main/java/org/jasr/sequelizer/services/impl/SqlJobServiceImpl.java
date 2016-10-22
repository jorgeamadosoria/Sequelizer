package org.jasr.sequelizer.services.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.jasr.sequelizer.dao.SqlJobRepository;
import org.jasr.sequelizer.entities.CSVExporterCountCallbackHandler;
import org.jasr.sequelizer.entities.ExecutionStatus;
import org.jasr.sequelizer.entities.SVNHandler;
import org.jasr.sequelizer.entities.SVNHandlerImpl;
import org.jasr.sequelizer.entities.SqlJob;
import org.jasr.sequelizer.services.SqlJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SqlJobServiceImpl implements SqlJobService {

    @Value("${spring.flush.count}")
    private int                                     flushCount;
    @Value("${spring.csv.folder}")
    private String                                  csvFolder;
    @Autowired
    private SqlJobRepository                        sqlJobRepository;
    @Resource
    private Map<String, NamedParameterJdbcTemplate> templates;
    @Value("${spring.svn.username}")
    private String                                  svnUsername;
    @Value("${spring.svn.password}")
    private String                                  svnPassword;
    @Value("${spring.svn.base}")
    private String                                  svnBase;

    @Override
    public ExecutionStatus execute(long id, Map<String, Object> parameters, boolean csvExport) {

        boolean success = true;
        SqlJob job = sqlJobRepository.findOne(id);

        try (CSVExporterCountCallbackHandler csvExporter = new CSVExporterCountCallbackHandler(job, csvFolder, flushCount)) {
            SVNHandler handler = new SVNHandlerImpl(job, csvFolder, svnBase, svnUsername, svnPassword);
            handler.pre(job);

            templates.get(job.getDbName()).query(job.getCode(), Collections.emptyMap(), csvExporter);

            job.setLastExecutionStatus(String.valueOf(csvExporter.getRowCount()));

            handler.post(job);
        }
        catch (Exception e) {
            e.printStackTrace();
            job.setLastExecutionStatus(e.getMessage());
            success = false;
        }
        finally {
            job.setLastExecutionDate(new Date());
            sqlJobRepository.save(job);
        }
        return new ExecutionStatus(success, job.getLastExecutionDate(), job.getLastExecutionStatus());
    }

    private String countWrap(String code) {
        return "select count(*) from (" + code + ") tbl" + new Date().getTime();
    }

    @Override
    public ExecutionStatus count(long id, Map<String, Object> parameters, boolean csvExport) {
        SqlJob job = sqlJobRepository.findOne(id);
        boolean success = true;

        try {
            Long rows = templates.get(job.getDbName()).queryForObject(countWrap(job.getCode()), Collections.emptyMap(),
                    Long.class);

            job.setLastExecutionStatus(String.valueOf(rows));
        }
        catch (Exception e) {
            job.setLastExecutionStatus(e.getMessage());
            success = false;
        }
        finally {
            job.setLastExecutionDate(new Date());
            sqlJobRepository.save(job);
        }
        return new ExecutionStatus(success, job.getLastExecutionDate(), job.getLastExecutionStatus());
    }
}
