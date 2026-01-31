package com.example.final_project.features.media;


import com.example.final_project.features.media.dto.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaResponse uploadSingle(MultipartFile file, String folderName);
}
