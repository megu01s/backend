package com.example.imageproc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class ImageProcApplication {

    private static final Logger log = LoggerFactory.getLogger(ImageProcApplication.class);

    @Value("${file.upload-dir:/home/${USER}/projects/imageproc/uploads}")
    private String uploadDir;

    public static void main(String[] args) {
        SpringApplication.run(ImageProcApplication.class, args);
    }

    /**
     * Ensure the upload directory exists at startup.
     */
    @org.springframework.context.annotation.Bean
    CommandLineRunner initUploadDir() {
        return args -> {
            try {
                Path path = Paths.get(uploadDir);
                Files.createDirectories(path);
                log.info("Upload directory ready at: {}", path.toAbsolutePath());
            } catch (Exception e) {
                log.error("Failed to create upload directory '{}': {}", uploadDir, e.getMessage());
                // You may choose to rethrow here if this is critical:
                // throw e;
            }
        };
    }
}
