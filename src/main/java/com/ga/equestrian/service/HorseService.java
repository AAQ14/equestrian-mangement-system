package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.HorseRequest;
import com.ga.equestrian.dto.request.UpdateHorseRequest;
import com.ga.equestrian.dto.response.HorseResponse;
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
import java.util.List;
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
            String oldImage = updatedHorse.getImage();
            String filename = fileStorageService.uploadFile(updateHorseRequest.getImage(),  fileStorageService.HORSES_FOLDER);
            updatedHorse.setImage(filename);
            if(oldImage!=null){
                fileStorageService.deleteImage(oldImage, FileStorageService.HORSES_FOLDER);
            }
        }
        return horseRepository.save(updatedHorse);
    }

    /**
     * Delete a horse by its ID.
     *
     * @param id the ID of the horse to delete.
     * @return the deleted horse.
     * @throws InformationNotFoundException if the horse is not found.
     */
    public Horse deleteHorse(Long id){
        Horse deletedHorse = horseRepository.findById(id).orElseThrow(
                ()-> new InformationNotFoundException("Horse with this " + id + " is not found.")
        );
        if(deletedHorse.getImage()!=null){
            fileStorageService.deleteImage(deletedHorse.getImage(),fileStorageService.HORSES_FOLDER );
        }
        horseRepository.delete(deletedHorse);
        return deletedHorse;
    }

    /**
     * Get all horses stored in the database.
     *
     * @return a list of all horses.
     * @throws InformationNotFoundException if there are no horses.
     */
    public List<HorseResponse> getAllHorses(){
        List<Horse> horses = horseRepository.findAll();

        if(horses.isEmpty()){
            throw new InformationNotFoundException("There are no horses");
        }
            return horses.stream().map(horse -> new HorseResponse(
                    horse.getId(),
                    horse.getName(),
                    horse.getBreed(),
                    horse.getAge(),
                    horse.getGender(),
                    horse.getHorseStatus(),
                    horse.getSkillLevel(),
                    horse.getImage()
            )).toList();
    }

    /**
     * Get horse by its ID.
     *
     * @param id the id of the horse.
     * @return the horse response.
     * @throws InformationNotFoundException if the horse is not found.
     */
    public HorseResponse getHorse(Long id){
        Horse horse = horseRepository.findById(id).orElseThrow(
                ()-> new InformationNotFoundException("Horse with this " + id + " is not found.")
        );
        return new HorseResponse(
                horse.getId(),
                horse.getName(),
                horse.getBreed(),
                horse.getAge(),
                horse.getGender(),
                horse.getHorseStatus(),
                horse.getSkillLevel(),
                horse.getImage());
    }
}
