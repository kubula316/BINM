package com.BINM.media.service;

import com.BINM.media.exception.MediaException;
import com.BINM.media.storage.ImageStorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
class MediaService implements MediaFacade {

    private final ImageStorageClient imageStorageClient;

    @Value("${azure.storage.container.media}")
    private String mediaContainerName;

    @Value("${azure.storage.container.profile}")
    private String profileContainerName;

    @Override
    public String UploadMediaImage(MultipartFile file) {
        try {
            return uploadImage(mediaContainerName, file);
        } catch (IOException e) {
            throw MediaException.uploadFailed(e);
        }
    }

    @Override
    public String uploadProfileImage(MultipartFile file) {
        try {
            return uploadImage(profileContainerName, file);
        } catch (IOException e) {
            throw MediaException.uploadFailed(e);
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
            throw MediaException.deleteFailed();
        }
    }
}
