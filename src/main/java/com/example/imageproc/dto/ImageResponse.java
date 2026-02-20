package com.example.imageproc.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImageResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private LocalDateTime uploadedAt;
    private String url;
    private UserResponse user;
}
