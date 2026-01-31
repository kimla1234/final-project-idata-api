package com.example.final_project.features.media;

import com.example.final_project.features.media.dto.MediaResponse;
import com.example.final_project.utils.MediaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    @Value("${media.server-path}")
    private String server_path;

    @Value("${media.base-uri}")
    private String baseUri;


    @Override
    public MediaResponse uploadSingle(MultipartFile file, String folderName) {
        String newName = UUID.randomUUID().toString();
        String extension = MediaUtil.extractExtension(Objects.requireNonNull(file.getOriginalFilename()));
        newName = newName + "." + extension;

        // 1. Create the directory path object
        Path folderPath = Paths.get(server_path, folderName);
        Path filePath = folderPath.resolve(newName);

        try {
            // 2. Automatically create the folder if it's missing
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file: " + e.getMessage());
        }

        return MediaResponse.builder()
                .name(newName)
                .contentType(file.getContentType())
                .extension(extension)
                .size(file.getSize())
                .uri(String.format("%s%s/%s", baseUri, folderName, newName))
                .build();
    }
}
