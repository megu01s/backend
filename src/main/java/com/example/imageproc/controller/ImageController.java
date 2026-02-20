package com.example.imageproc.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.imageproc.dto.ImageResponse;
import com.example.imageproc.dto.UserResponse;
import com.example.imageproc.entity.ImageEntity;
import com.example.imageproc.service.ImageService;
import com.example.imageproc.util.ImageProcessingUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // ==========================
    // DTO MAPPER
    // ==========================
    private ImageResponse toResponse(ImageEntity img) {
        return ImageResponse.builder()
                .id(img.getId())
                .fileName(img.getFileName())
                .fileType(img.getFileType())
                .uploadedAt(img.getUploadedAt())
                .url("/api/images/" + img.getId())
                .user(
                        UserResponse.builder()
                                .id(img.getUser().getId())
                                .username(img.getUser().getUsername())
                                .email(img.getUser().getEmail())
                                .build()
                )
                .build();
    }

    // ==========================
    // IMAGE UPLOAD
    // ==========================
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> upload(@RequestPart("file") MultipartFile file) {
        ImageEntity saved = imageService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // ==========================
    // GET RAW IMAGE (bytes)
    // ==========================
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        Resource file = imageService.loadAsResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    // ==========================
    // LIST IMAGES (Current User)
    // ==========================
    @GetMapping
    public ResponseEntity<List<ImageResponse>> list() {
        var responses = imageService.listForCurrentUser().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    // ==========================
    // DELETE IMAGE
    // ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        imageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================
    // TRANSFORMATIONS
    // ==========================
    // RESIZE
    @PostMapping("/{id}/resize")
    public ResponseEntity<ImageResponse> resize(
            @PathVariable Long id,
            @RequestParam int width,
            @RequestParam int height,
            @RequestParam(defaultValue = "true") boolean keepAspect
    ) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("resized-" + entity.getFileName());

        try {
            ImageProcessingUtil.resize(input, output, width, height, keepAspect);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // CROP
    @PostMapping("/{id}/crop")
    public ResponseEntity<ImageResponse> crop(
            @PathVariable Long id,
            @RequestParam int x,
            @RequestParam int y,
            @RequestParam int width,
            @RequestParam int height
    ) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("crop-" + entity.getFileName());

        try {
            ImageProcessingUtil.crop(input, output, x, y, width, height);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ROTATE
    @PostMapping("/{id}/rotate")
    public ResponseEntity<ImageResponse> rotate(
            @PathVariable Long id,
            @RequestParam double degrees
    ) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("rot-" + entity.getFileName());

        try {
            ImageProcessingUtil.rotate(input, output, degrees);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // FLIP
    @PostMapping("/{id}/flip")
    public ResponseEntity<ImageResponse> flip(
            @PathVariable Long id,
            @RequestParam(defaultValue = "H") String direction
    ) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("flip-" + entity.getFileName());

        try {
            if ("V".equalsIgnoreCase(direction)) {
                ImageProcessingUtil.flipVertical(input, output);
            } else {
                ImageProcessingUtil.flipHorizontal(input, output);
            }
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // MIRROR
    @PostMapping("/{id}/mirror")
    public ResponseEntity<ImageResponse> mirror(@PathVariable Long id) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("mirror-" + entity.getFileName());

        try {
            ImageProcessingUtil.mirror(input, output);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // WATERMARK TEXT
    @PostMapping("/{id}/watermark/text")
    public ResponseEntity<ImageResponse> watermarkText(
            @PathVariable Long id,
            @RequestParam String text,
            @RequestParam(defaultValue = "0.35") float alpha,
            @RequestParam(defaultValue = "24") int fontSize
    ) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("wm-" + entity.getFileName());

        try {
            ImageProcessingUtil.watermarkText(input, output, text, alpha, fontSize, null, 12, 12);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // COMPRESS
    @PostMapping("/{id}/compress")
    public ResponseEntity<ImageResponse> compress(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0.7") float quality
    ) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("cmp-" + entity.getFileName());

        try {
            ImageProcessingUtil.compress(input, output, quality);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // CHANGE FORMAT
    @PostMapping("/{id}/format")
    public ResponseEntity<ImageResponse> changeFormat(
            @PathVariable Long id,
            @RequestParam String format
    ) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());

        String baseName = entity.getFileName().replaceAll("\\.[^.]+$", "");
        Path output = input.getParent().resolve(baseName + "." + format.toLowerCase());

        try {
            ImageProcessingUtil.changeFormat(input, output, format.toLowerCase());
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // FILTER
    @PostMapping("/{id}/filter")
    public ResponseEntity<ImageResponse> applyFilter(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam(defaultValue = "25") int intensity
    ) {
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("filter-" + entity.getFileName());

        try {
            switch (type.toLowerCase()) {
                case "grayscale" ->
                    ImageProcessingUtil.grayscale(input, output);
                case "sepia" ->
                    ImageProcessingUtil.sepia(input, output, intensity);
                default -> {
                    return ResponseEntity.badRequest().build();
                }
            }
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
