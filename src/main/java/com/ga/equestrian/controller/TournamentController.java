package com.ga.equestrian.controller;

import com.ga.equestrian.model.entity.Tournament;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

//    @PreAuthorize(value = "admin")
//    @PostMapping
//    public EntityResponse<Tournament> addTournament(@Par){}
}
