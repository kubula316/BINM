package com.example.media.service;

import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    String UploadMediaImage(MultipartFile file);

    String uploadProfileImage(MultipartFile file);
}
