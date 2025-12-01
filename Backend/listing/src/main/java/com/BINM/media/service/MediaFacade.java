package com.BINM.media.service;

import org.springframework.web.multipart.MultipartFile;

public interface MediaFacade {
    String UploadMediaImage(MultipartFile file);

    String uploadProfileImage(MultipartFile file);
}
