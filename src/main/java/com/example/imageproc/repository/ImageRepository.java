package com.example.imageproc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.imageproc.entity.ImageEntity;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    List<ImageEntity> findByUserId(Long userId);
}
