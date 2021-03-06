package com.hwr.hwrzeiterfassung.database.controller;

import com.hwr.hwrzeiterfassung.database.repositorys.RoleRepository;
import com.hwr.hwrzeiterfassung.database.tables.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for the roles
 */
@Controller
@RequestMapping(path = "/role")
public class RoleController {
    @Autowired
    private RoleRepository roleRepository;

    /**
     * get all existing roles
     *
     * @return List of roles
     */
    @GetMapping(path = "/getAllRoles")
    public @ResponseBody
    Iterable<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
