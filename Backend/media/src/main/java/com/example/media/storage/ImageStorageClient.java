package com.example.media.storage;

import java.io.IOException;
import java.io.InputStream;

public interface ImageStorageClient {
    String uploadImage(String containerName, String originalImageName , InputStream data, long length)throws IOException;
    void deleteImage(String containerName, String oldImageUrl) throws IOException;
}
