package com.example.imageproc.mapper;

import org.springframework.stereotype.Component;

import com.example.imageproc.dto.ImageResponse;
import com.example.imageproc.dto.UserResponse;
import com.example.imageproc.entity.ImageEntity;
import com.example.imageproc.entity.UserEntity;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Builder
@Component
@Slf4j
public class ImageMapper {

    public ImageResponse toResponse(ImageEntity entity) {
        if (entity == null) {
            return null;
        }

        UserResponse userResponse = null;
        if (entity.getUser() != null) {
            UserEntity user = entity.getUser();
            userResponse = UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();
        } else {
            log.warn("ImageEntity with id {} has no associated user", entity.getId());
        }

        return ImageResponse.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .uploadedAt(entity.getUploadedAt())
                .url("/api/images/" + entity.getId())
                .user(userResponse)
                .build();
    }
}
