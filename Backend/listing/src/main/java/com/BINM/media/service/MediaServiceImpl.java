package com.BINM.media.service;


import com.BINM.media.exception.UploadError;
import com.BINM.media.exception.UploadException;
import com.BINM.media.storage.ImageStorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final ImageStorageClient imageStorageClient;

    private static final String MEDIA_CONTAINER_NAME = "media-image-container";
    private static final String PROFILE_CONTAINER_NAME = "profile-image-container";

    @Override
    public String UploadMediaImage(MultipartFile file) {
        try {
            return uploadImage(MEDIA_CONTAINER_NAME, file);
        } catch (IOException e) {
            throw new UploadException(UploadError.FAILED_TO_UPLOAD_IMAGE);
        }
    }

    @Override
    public String uploadProfileImage(MultipartFile file) {
        try {
            return uploadImage(PROFILE_CONTAINER_NAME, file);
        } catch (IOException e) {
            throw new UploadException(UploadError.FAILED_TO_UPLOAD_IMAGE);
        }
    }

    public String uploadImage(String containerName, MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return this.imageStorageClient.uploadImage(containerName, file.getOriginalFilename(), inputStream, file.getSize());
        }
    }

    public void deleteImage(String containerName, String url) throws IOException {
        try {
            this.imageStorageClient.deleteImage(containerName, url);
        } catch (Exception e) {
            throw new UploadException(UploadError.FAILED_TO_DELETE_IMAGE);
        }
    }
}
