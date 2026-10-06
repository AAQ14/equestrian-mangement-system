package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.HorseRequest;
import com.ga.equestrian.dto.request.UpdateHorseRequest;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.model.entity.Horse;
import com.ga.equestrian.repository.HorseRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@AllArgsConstructor
public class HorseService {

    private FileStorageService fileStorageService;

    private HorseRepository horseRepository;

    /**
     *
     * @param horseRequest
     * @return
     */
    public Horse createHorse(HorseRequest horseRequest){
        Horse newHorse = new Horse();
        newHorse.setName(horseRequest.getName());
        newHorse.setBreed(horseRequest.getBreed());
        newHorse.setAge(horseRequest.getAge());
        newHorse.setGender(horseRequest.getGender());
        newHorse.setHorseStatus(horseRequest.getHorseStatus());
        newHorse.setSkillLevel(horseRequest.getSkillLevel());

        if(horseRequest.getImage()!=null){
            String filename = fileStorageService.uploadFile(horseRequest.getImage(),  fileStorageService.HORSES_FOLDER);
            newHorse.setImage(filename);
        }
        return horseRepository.save(newHorse);
    }

    public Horse updateHorse(Long id, UpdateHorseRequest updateHorseRequest){
        Horse updatedHorse = horseRepository.findById(id).orElseThrow(
                ()->new InformationNotFoundException("Horse with this id is " + id + " is not found" )
        );

        if (updateHorseRequest.getName() != null) {
            updatedHorse.setName(updateHorseRequest.getName());
        }

        if (updateHorseRequest.getBreed() != null) {
            updatedHorse.setBreed(updateHorseRequest.getBreed());
        }

        if (updateHorseRequest.getAge() != null) {
            updatedHorse.setAge(updateHorseRequest.getAge());
        }

        if (updateHorseRequest.getGender() != null) {
            updatedHorse.setGender(updateHorseRequest.getGender());
        }

        if (updateHorseRequest.getHorseStatus() != null) {
            updatedHorse.setHorseStatus(updateHorseRequest.getHorseStatus());
        }

        if (updateHorseRequest.getSkillLevel() != null) {
            updatedHorse.setSkillLevel(updateHorseRequest.getSkillLevel());
        }

        if(updateHorseRequest.getImage()!=null){
            String filename = fileStorageService.uploadFile(updateHorseRequest.getImage(),  fileStorageService.HORSES_FOLDER);
            updatedHorse.setImage(filename);
        }
        return horseRepository.save(updatedHorse);

    }
}
