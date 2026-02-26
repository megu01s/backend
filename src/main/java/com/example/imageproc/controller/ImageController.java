package com.example.imageproc.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.imageproc.dto.ErrorResponse;
import com.example.imageproc.dto.ImageResponse;
import com.example.imageproc.entity.ImageEntity;
import com.example.imageproc.mapper.ImageMapper;
import com.example.imageproc.service.ImageService;
import com.example.imageproc.util.ImageProcessingUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageMapper imageMapper;

    // ==========================
    // IMAGE UPLOAD
    // ==========================
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> upload(@RequestParam("file") MultipartFile file) {
        // 1) Validation du content-type (MIME)
        final String contentType = file.getContentType();
        if (contentType == null
                || !(MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)
                || MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType))) {

            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Invalid image type")
                            .build());
        }

        // 2) Validation que c'est bien une image (défense en profondeur)
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) {
                return ResponseEntity
                        .badRequest()
                        .body(ErrorResponse.builder()
                                .error("Bad Request")
                                .message("Invalid image file")
                                .build());
            }
        } catch (IOException e) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Invalid image file")
                            .build());
        }

        // 3) Traitement normal
        ImageEntity saved = imageService.upload(file);
        ImageResponse body = imageMapper.toResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // ==========================
    // GET RAW IMAGE (bytes)
    // ==========================
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {

        try {
            Resource file = imageService.loadAsResource(id);

            // 404 si pas trouvé, ou invalide
            if (file == null || !file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Succès → 200 + bytes
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ==========================
    // LIST IMAGES (Current User)
    // ==========================
    @GetMapping
    public ResponseEntity<List<ImageResponse>> list() {
        var responses = imageService.listForCurrentUser().stream()
                .map(imageMapper::toResponse)
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
    public ResponseEntity<Object> resize(
            @PathVariable Long id,
            @RequestParam int width,
            @RequestParam int height,
            @RequestParam(defaultValue = "true") boolean keepAspect
    ) {

        // -------- VALIDATION --------
        if (width <= 0 || height <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Width and height must be positive integers")
                            .build()
                    );
        }

        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("resized-" + entity.getFileName());

        try {
            ImageProcessingUtil.resize(input, output, width, height, keepAspect);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(imageMapper.toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // CROP
    @PostMapping("/{id}/crop")
    public ResponseEntity<Object> crop(
            @PathVariable Long id,
            @RequestParam int x,
            @RequestParam int y,
            @RequestParam int width,
            @RequestParam int height
    ) {
        // -------- VALIDATION DES PARAMÈTRES --------
        if (x < 0 || y < 0 || width <= 0 || height <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Crop region must be positive and non-negative coordinates")
                            .build()
                    );
        }

        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("crop-" + entity.getFileName());

        try {
            ImageProcessingUtil.crop(input, output, x, y, width, height);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(imageMapper.toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ROTATE
    @PostMapping("/{id}/rotate")
    public ResponseEntity<Object> rotate(
            @PathVariable Long id,
            @RequestParam double degrees
    ) {

        // Validation
        if (degrees < 0 || degrees > 360) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Degrees must be between 0 and 360")
                            .build());
        }

        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("rot-" + entity.getFileName());

        try {
            ImageProcessingUtil.rotate(input, output, degrees);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(imageMapper.toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Unable to rotate image")
                            .build()
            );
        }
    }

    // FLIP
    @PostMapping("/{id}/flip")
    public ResponseEntity<Object> flip(
            @PathVariable Long id,
            @RequestParam(defaultValue = "H") String direction
    ) {
        // -------- VALIDATION --------
        if (!direction.equalsIgnoreCase("H") && !direction.equalsIgnoreCase("V")) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Invalid flip mode. Allowed values are H or V.")
                            .build()
                    );
        }

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
            return ResponseEntity.ok(imageMapper.toResponse(variant));

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
            return ResponseEntity.ok(imageMapper.toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // WATERMARK TEXT
    @PostMapping("/{id}/watermark/text")
    public ResponseEntity<Object> watermarkText(
            @PathVariable Long id,
            @RequestParam String text,
            @RequestParam(defaultValue = "0.35") float alpha,
            @RequestParam(defaultValue = "24") int fontSize
    ) {

        // -------- VALIDATION --------
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Watermark text cannot be empty")
                            .build()
            );
        }

        if (text.length() > 40) {  // tu peux ajuster cette limite
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Watermark text is too long")
                            .build()
            );
        }

        if (alpha <= 0.0f || alpha > 1.0f) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Alpha must be between 0.0 and 1.0")
                            .build()
            );
        }

        if (fontSize < 1) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Font size must be >= 1")
                            .build()
            );
        }

        // -------- PROCESSING --------
        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("wm-" + entity.getFileName());

        try {
            ImageProcessingUtil.watermarkText(input, output, text, alpha, fontSize, null, 12, 12);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(imageMapper.toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Failed to apply watermark")
                            .build()
            );
        }
    }

    // COMPRESS
    @PostMapping("/{id}/compress")
    public ResponseEntity<Object> compress(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0.7") float quality
    ) {

        // ------- VALIDATION -------
        if (quality <= 0.0f || quality > 1.0f) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Quality must be between 0.0 and 1.0")
                            .build()
            );
        }

        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());
        Path output = input.getParent().resolve("cmp-" + entity.getFileName());

        try {
            ImageProcessingUtil.compress(input, output, quality);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(imageMapper.toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Unable to compress image")
                            .build()
            );
        }
    }

    // CHANGE FORMAT
    @PostMapping("/{id}/format")
    public ResponseEntity<Object> changeFormat(
            @PathVariable Long id,
            @RequestParam String format
    ) {

        // -------- VALIDATION --------
        if (format == null || format.isBlank()) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Format must not be empty")
                            .build()
            );
        }

        String normalized = format.toLowerCase();

        // Formats supported by your backend
        List<String> allowedFormats = List.of("png", "jpg", "jpeg");

        if (!allowedFormats.contains(normalized)) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Unsupported format: " + format + ". Allowed formats are png, jpg, jpeg.")
                            .build()
            );
        }

        var entity = imageService.findById(id);
        Path input = Paths.get(entity.getFilePath());

        String baseName = entity.getFileName().replaceAll("\\.[^.]+$", "");
        Path output = input.getParent().resolve(baseName + "." + normalized);

        try {
            ImageProcessingUtil.changeFormat(input, output, normalized);
            var variant = imageService.registerVariant(entity, output);
            return ResponseEntity.ok(imageMapper.toResponse(variant));

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("Bad Request")
                            .message("Unable to convert image to format: " + format)
                            .build()
            );
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
            return ResponseEntity.ok(imageMapper.toResponse(variant));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
