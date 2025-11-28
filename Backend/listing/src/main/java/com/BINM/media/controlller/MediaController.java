package com.BINM.media.controlller;


import com.BINM.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/media-image")
    @ResponseStatus(HttpStatus.CREATED)
    public String updateImage(@RequestParam MultipartFile file) {
        return mediaService.UploadMediaImage(file);
    }

    @PostMapping("/profile-image")
    @ResponseStatus(HttpStatus.CREATED)
    public String updateProfileImage(@RequestParam MultipartFile file) {
        return mediaService.uploadProfileImage(file);
    }
}
