package com.example.imageproc.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.imageproc.dto.ImageResponse;
import com.example.imageproc.dto.UserResponse;
import com.example.imageproc.entity.ImageEntity;
import com.example.imageproc.repository.ImageRepository;
import com.example.imageproc.service.AuthService;
import com.example.imageproc.service.ImageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final AuthService authService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public ImageEntity upload(MultipartFile file) {

        try {
            // 1. Secure filename
            String originalName = StringUtils.cleanPath(file.getOriginalFilename());

            if (originalName.contains("..")) {
                throw new RuntimeException("Invalid file path");
            }

            // 2. Prepare directory
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            // 3. Unique filename
            String uniqueName = UUID.randomUUID() + "-" + originalName;
            Path targetPath = uploadPath.resolve(uniqueName);

            // 4. Save file to WSL filesystem
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 5. Save metadata
            ImageEntity entity = new ImageEntity();
            entity.setFileName(originalName);
            entity.setFileType(file.getContentType());
            entity.setFilePath(targetPath.toString());
            entity.setUser(authService.getCurrentUser());

            return imageRepository.save(entity);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public Resource loadAsResource(Long imageId) {
        ImageEntity entity = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        try {
            Path filePath = Paths.get(entity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error loading file", e);
        }
    }

    @Override
    public List<ImageEntity> listForCurrentUser() {
        Long userId = authService.getCurrentUser().getId();
        return imageRepository.findByUserId(userId);
    }

    @Override
    public void delete(Long imageId) {
        ImageEntity entity = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        try {
            // Remove file from disk
            Files.deleteIfExists(Paths.get(entity.getFilePath()));
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file", ex);
        }

        imageRepository.delete(entity);
    }

    @Override
    public ImageEntity findById(Long id) {
        // Fetch the image from the database or throw a clear error
        return imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
    }

    @Override
    public ImageEntity registerVariant(ImageEntity originalImage, Path outputPath) {
        // Create a new ImageEntity that points to the transformed file
        ImageEntity variant = new ImageEntity();

        // File name is the last segment of the output path
        String newFileName = outputPath.getFileName().toString();

        variant.setFileName(newFileName);
        variant.setFileType(inferContentType(outputPath));     // helper method below
        variant.setFilePath(outputPath.toAbsolutePath().toString());

        // Keep the same owner/user as the original image
        variant.setUser(originalImage.getUser() != null
                ? originalImage.getUser()
                : authService.getCurrentUser());

        // Persist the new variant
        return imageRepository.save(variant);
    }

    private String inferContentType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        if (name.endsWith(".bmp")) {
            return "image/bmp";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    private ImageResponse toResponse(ImageEntity entity) {
        return ImageResponse.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .uploadedAt(entity.getUploadedAt())
                .url("/api/images/" + entity.getId())
                .user(
                        UserResponse.builder()
                                .id(entity.getUser().getId())
                                .username(entity.getUser().getUsername())
                                .email(entity.getUser().getEmail())
                                .build()
                )
                .build();
    }

}
