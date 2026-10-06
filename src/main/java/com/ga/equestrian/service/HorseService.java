package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.HorseRequest;
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
}
