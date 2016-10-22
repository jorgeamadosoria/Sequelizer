package org.jasr.sequelizer.controller;

import org.jasr.sequelizer.dao.SqlJobRepository;
import org.jasr.sequelizer.entities.SqlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("pages")
public class PagesController {

    @Autowired
    private SqlJobRepository                        sqlJobRepository;

    
    @RequestMapping("/jobs")
    public String jobsList(Model model) {
        
        Iterable<SqlJob> jobs = sqlJobRepository.findAll();
        model.addAttribute("jobs", jobs);
        return "jobs";
    }
    
}