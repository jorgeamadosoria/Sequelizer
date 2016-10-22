package org.jasr.sequelizer.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.jasr.sequelizer.dao.SqlJobRepository;
import org.jasr.sequelizer.entities.ExecutionStatus;
import org.jasr.sequelizer.entities.SqlJob;
import org.jasr.sequelizer.services.SqlJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("actions")
public class RestActionsController {

    @Resource
    private SqlJobService                           sqlJobService;
    @Resource
    private Map<String, NamedParameterJdbcTemplate> templates;
    @Value("${spring.svn.base}")
    private String                                  svnBase;
    @Autowired
    private SqlJobRepository                        sqlJobRepository;
    
    private AtomicReference<Set<Long>>              executionLock = new AtomicReference<>(new HashSet<>());

    private ResponseEntity<ExecutionStatus> doExecute(long id, boolean count) {
        ExecutionStatus status = new ExecutionStatus(false, new Date(), "Job is being executed already");
        if (!executionLock.get().contains(id)) {
            executionLock.get().add(id);
            status = count ? sqlJobService.count(id, null, true) : sqlJobService.execute(id, null, true);
            executionLock.get().remove(id);
        }

        return new ResponseEntity<ExecutionStatus>(status, status.isSuccess() ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST);

    }

    @RequestMapping("/sqljobs/execute/{id}")
    public ResponseEntity<ExecutionStatus> sqlJobsExecute(@PathVariable long id) {
        return doExecute(id, false);
    }
    
    @RequestMapping("/sqljobs/execute/all")
    public void sqlJobsExecute() {
        
        Iterable<SqlJob> jobs = sqlJobRepository.findAll();
        
        for(SqlJob job: jobs){
            doExecute(job.getId(),false);
        }
    }

    @RequestMapping("/sqljobs/count/{id}")
    public ResponseEntity<ExecutionStatus> sqlJobsCount(@PathVariable long id) {
        return doExecute(id, true);
    }

    @RequestMapping("/properties/list")
    public ResponseEntity<Map<String, String>> properties() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("svnBase", svnBase);
        return new ResponseEntity<Map<String, String>>(map, HttpStatus.ACCEPTED);
    }

    @RequestMapping("/dataSources/list")
    public Set<String> dataSources() {
        return templates.keySet();
    }
}
