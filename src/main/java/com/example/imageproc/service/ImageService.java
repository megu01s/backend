package com.example.imageproc.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.example.imageproc.entity.ImageEntity;

public interface ImageService {

    ImageEntity upload(MultipartFile file);

    Resource loadAsResource(Long imageId);

    List<ImageEntity> listForCurrentUser();

    void delete(Long imageId);

    ImageEntity findById(Long id);

    ImageEntity registerVariant(ImageEntity originalImage, java.nio.file.Path outputPath);
}
