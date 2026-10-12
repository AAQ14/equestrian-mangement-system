package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.UpdateUserRequest;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidUserActionException;
import com.ga.equestrian.mapper.UserMapper;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.UserStatus;
import com.ga.equestrian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ga.equestrian.exception.InvalidFileException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    /**
     * Updates the profile of the signed-in user.
     *
     * @param email the email of the signed-in user, taken from the JWT.
     * @param request the fields to change.
     * @return the updated user.
     * @throws InformationNotFoundException if the user does not exist.
     * @throws InvalidFileException if the picture is empty, too large or not an allowed image type.
     */
    @Transactional
    public UserResponse updateOwnProfile(String email, UpdateUserRequest request){
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with email " + email + " not found.")
        );
        applyProfileUpdate(user, request);
        log.info("User {} updated their profile", user.getId());
        return userMapper.toResponse(user);
    }

    /**
     * Updates the profile of any user. Only admins can reach this method.
     *
     * @param adminEmail the email of the signed-in admin, taken from the JWT.
     * @param id the id of the user to update.
     * @param request the fields to change.
     * @return the updated user.
     * @throws InformationNotFoundException if the admin or the user does not exist.
     * @throws InvalidFileException if the picture is empty, too large or not an allowed image type.
     */
    @Transactional
    public UserResponse updateUser(String adminEmail, Long id, UpdateUserRequest request){
        User admin = userRepository.findByEmail(adminEmail).orElseThrow(
                () -> new InformationNotFoundException("User with email " + adminEmail + " not found."));
        User user = userRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("User with id " + id + " not found."));
        applyProfileUpdate(user, request);
        log.info("Admin {} updated user {}", admin.getId(), user.getId());
        return userMapper.toResponse(user);
    }

    /**
     * Returns all users as response DTOs, optionally filtered by status.
     *
     * @param userStatus the status to filter by, or null for all users.
     * @return the list of users, empty if there are none.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(UserStatus userStatus){
        List<User> users;
        if (userStatus != null) {
            users = userRepository.findByUserStatus(userStatus);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(userMapper::toResponse).toList();
    }

    /**
     * Returns one user by id.
     *
     * @param id the user id.
     * @return the user as a response DTO.
     * @throws InformationNotFoundException if no user has this id.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(
                ()->new InformationNotFoundException("User with id " + id + " not found.")
        );
        return userMapper.toResponse(user);
    }


    /**
     * Applies the fields that were sent. A new picture replaces the old one,
     * and the old file is deleted only after the new one is saved.
     *
     * @param user the user to change.
     * @param request the fields to change; null fields are left unchanged.
     */
    private void applyProfileUpdate(User user, UpdateUserRequest request){
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if(request.getImage() != null){
            String oldPicture = user.getProfilePicture();
            String fileName = fileStorageService.uploadFile(request.getImage(), FileStorageService.USERS_FOLDER);
            user.setProfilePicture(fileName);
            if(oldPicture!=null){
                fileStorageService.deleteImage(oldPicture, FileStorageService.USERS_FOLDER);
            }
        }
    }


    /**
     * Deactivates a user (soft delete). The row is kept and the status becomes INACTIVE.
     *
     * @param adminEmail the email of the signed-in admin, taken from the JWT.
     * @param id the id of the user to deactivate.
     * @return the deactivated user.
     * @throws InformationNotFoundException if the admin or the user does not exist.
     * @throws InvalidUserActionException if the admin targets themselves or the user is already inactive.
     */
    @Transactional
    public UserResponse deactivateUser(String adminEmail, Long id) {
        User admin = userRepository.findByEmail(adminEmail).orElseThrow(
                () -> new InformationNotFoundException("User with email " + adminEmail + " not found.")
        );
        User user = userRepository.findById(id).orElseThrow(
                ()-> new InformationNotFoundException("User with id " + id + " not found.")
        );
        if(user.getUserStatus() == UserStatus.INACTIVE){
            throw new InvalidUserActionException("User with id " + id + " is already inactive.");
        }
        user.setUserStatus(UserStatus.INACTIVE);
        log.info("Admin {} deactivated user {}", admin.getId(), user.getId());
        return userMapper.toResponse(user);
    }


    /**
     * Reactivates an inactive user. The status goes back to ACTIVE.
     *
     * @param adminEmail the email of the signed-in admin, taken from the JWT.
     * @param id the id of the user to reactivate.
     * @return the reactivated user.
     * @throws InformationNotFoundException if the admin or the user does not exist.
     * @throws InvalidUserActionException if the user is not inactive.
     */
    @Transactional
    public UserResponse activateUser(String adminEmail, Long id){
        User admin = userRepository.findByEmail(adminEmail).orElseThrow(
                () -> new InformationNotFoundException("User with email " + adminEmail + " not found.")
        );
        User user = userRepository.findById(id).orElseThrow(
                ()-> new InformationNotFoundException("User with id " + id + " not found.")
        );
        if(user.getUserStatus() != UserStatus.INACTIVE){
            throw new InvalidUserActionException("Only inactive users can be reactivated.");
        }
        user.setUserStatus(UserStatus.ACTIVE);
        log.info("Admin {} reactivated user {}", admin.getId(), user.getId());
        return userMapper.toResponse(user);
    }
}
