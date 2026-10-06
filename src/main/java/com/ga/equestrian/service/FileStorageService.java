package com.ga.equestrian.service;

import com.ga.equestrian.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file-uploads-dir}")
    private String uploadDir;

    public static final String HORSES_FOLDER = "horses";
    public static final String USERS_FOLDER = "users";

    private final static Set<String> ALLOWED_FILE_TYPES =Set.of("image/jpeg", "image/png", "image/webp");

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    /**
     * checks an uploaded image and saves it in the given sub-folder.
     *
     * @param file  the uploaded image.
     * @param folder the sub-folder to save it in.
     * @return the generated file name, to be stored in the database.
     */
    public String uploadFile(MultipartFile file, String folder){
        validate(file);

        String fileName = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        Path uploadPath = Path.of(uploadDir, folder).toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(fileName).normalize();

        if(!filePath.startsWith(uploadPath)){
            throw new InvalidFileException("Invalid file name");
        }

        try {
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            file.transferTo(filePath);
            return fileName;
        }catch (IOException ex){
            throw new IllegalStateException("could not save image", ex);
        }

    }


    /**
     * Rejects empty files, files they are too large, and files whose
     * content type or extension is not an allowed image type
     *
     * @param file the original file uploaded.
     */
    private void validate(MultipartFile file) {
        if(file==null || file.isEmpty()){
            throw new InvalidFileException("File is required");
        }
        if(file.getSize() >  MAX_FILE_SIZE){
            throw new InvalidFileException("File is too large(maximum 2 MB)");
        }

        String contentType = file.getContentType();
        if(contentType == null || !ALLOWED_FILE_TYPES.contains(contentType)){
            throw new InvalidFileException("Only JPEG, PNG and WebP images are allowed");
        }

        if(!ALLOWED_EXTENSIONS.contains(getExtension(file.getOriginalFilename()))){
            throw new InvalidFileException("Only JPEG, PNG and WebP images are allowed");
        }
    }


    /**
     * Returns the lowercase text after the last dot of a file name,
     * or an empty text when there is none,
     *
     * @param fileName the original file name, which may be null.
     * @return         the extension, such as "png"
     */
    private String getExtension(String fileName){
        if(fileName==null){
            return "";
        }
        int dotIndex = fileName.lastIndexOf(".");
        if(dotIndex == -1){
            return "";
        }
        return fileName.substring(dotIndex+1).toLowerCase();
    }
}