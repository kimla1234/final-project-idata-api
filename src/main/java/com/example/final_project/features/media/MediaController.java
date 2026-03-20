package com.example.final_project.features.media;

import com.example.final_project.features.media.dto.MediaResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/media")
public class MediaController {
    private final MediaService mediaService;

    // 🎯 ថែម consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    @Operation(summary = "Upload a single image file")
    @PostMapping(value = "/upload-image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    MediaResponse uploadSingle(
            @RequestPart("file") MultipartFile file // 🎯 បញ្ជាក់ឈ្មោះ Part ឱ្យច្បាស់
    ) {
        return mediaService.uploadSingle(file, "IMAGE");
    }
}