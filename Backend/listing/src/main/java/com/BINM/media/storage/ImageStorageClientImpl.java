package com.BINM.media.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.BINM.media.exception.UploadError;
import com.BINM.media.exception.UploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageClientImpl implements ImageStorageClient{

    private final BlobServiceClient blobServiceClient;

    @Override
    public String uploadImage(String containerName, String originalImageName, InputStream data, long length) throws IOException {
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            String newImageName = UUID.randomUUID().toString()+ originalImageName.substring(originalImageName.lastIndexOf("."));
            BlobClient blobClient = blobContainerClient.getBlobClient(newImageName);
            blobClient.upload(data, length, true);
            return blobClient.getBlobUrl();
        }catch (BlobStorageException e){
            throw new UploadException(UploadError.FAILED_TO_UPLOAD_IMAGE);
        }
    }


    public void deleteImage(String containerName, String oldImageUrl){
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (oldImageUrl != null && !oldImageUrl.isEmpty() && !oldImageUrl.equals("base64Image")) {
                String oldImageName = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
                BlobClient oldBlobClient = blobContainerClient.getBlobClient(oldImageName);
                if (oldBlobClient.exists()) {
                    oldBlobClient.delete();
                }
            }
        }catch (Exception e){
            throw new UploadException(UploadError.FAILED_TO_DELETE_IMAGE);
        }
    }
}
