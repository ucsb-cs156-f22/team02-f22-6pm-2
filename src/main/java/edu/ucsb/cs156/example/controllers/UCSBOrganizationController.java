package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Api(description = "UCSBOrganization")
@RequestMapping("/api/UCSBOrganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {
    
    @Autowired
    UCSBOrganizationRepository ucsbOrganizationRepository;

    //Functionality for retrieving all data base values in JSON format
    @ApiOperation(value = "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allOrganizations() {
        Iterable<UCSBOrganization> orgs = ucsbOrganizationRepository.findAll();
        return orgs;
    }

    //Functionality for posting an organization to the database. 
    @ApiOperation(value = "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganization postOrganization(
            @ApiParam("orgCode") @RequestParam String orgCode,
            @ApiParam("orgTranslationShort") @RequestParam String orgTranslationShort,
            @ApiParam("orgTranslation") @RequestParam String orgTranslation,
            @ApiParam("inactive") @RequestParam boolean inactive)
            throws JsonProcessingException {

        log.info("orgCode={}", orgCode);
        log.info("orgTranslationShort={}", orgTranslationShort);
        log.info("orgTranslation={}", orgTranslation);
        log.info("inactive={}", inactive);

        UCSBOrganization ucsbOrganization = new UCSBOrganization();
        ucsbOrganization.setOrgCode(orgCode);
        ucsbOrganization.setOrgTranslationShort(orgTranslationShort);
        ucsbOrganization.setOrgTranslation(orgTranslation);
        ucsbOrganization.setInactive(inactive);

        UCSBOrganization savedUcsbOrganization = ucsbOrganizationRepository.save(ucsbOrganization);

        return savedUcsbOrganization;
    }

    //Functionality to retrieve a single entry with a code. 
    @ApiOperation(value = "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @ApiParam("orgCode") @RequestParam String orgCode) {
        UCSBOrganization ucsbOrganization = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        return ucsbOrganization;
    }

    //Functionality to delete an entry in the table
    @ApiOperation(value = "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteCommons(
            @ApiParam("orgCode") @RequestParam String orgCode) {
        UCSBOrganization commons = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        ucsbOrganizationRepository.delete(commons);
        return genericMessage("UCSBOrganization with id %s deleted".formatted(orgCode));
    }

    //Functionality to edit entry in table
    @ApiOperation(value = "Update a single organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganization updateCommons(
            @ApiParam("orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganization incoming) {

        UCSBOrganization orgs = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        orgs.setOrgTranslationShort(incoming.getOrgTranslationShort());
        orgs.setOrgTranslation(incoming.getOrgTranslation());
        orgs.setInactive(incoming.getInactive());

        ucsbOrganizationRepository.save(orgs);

        return orgs;
    }
}
