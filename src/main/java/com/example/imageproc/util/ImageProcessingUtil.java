package com.example.imageproc.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public final class ImageProcessingUtil {

    private ImageProcessingUtil() {
    }

    /* =========================
    Core helpers (read/write)
       ========================= */
    private static BufferedImage read(Path input) throws IOException {
        try (var in = Files.newInputStream(input)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new IOException("Unsupported image format: " + input);
            }
            return img;
        }
    }

    private static void write(BufferedImage img, String format, Path output) throws IOException {
        Files.createDirectories(output.getParent());

        // Make sure the file actually gets written
        boolean ok;
        try (var out = Files.newOutputStream(output)) {
            ok = ImageIO.write(img, format.toLowerCase(), out);
        }

        if (!ok) {
            throw new IOException("❌ ImageIO failed to write image. Unsupported or mismatched format: " + format);
        }

        System.out.println("✔ Image written successfully to: " + output.toAbsolutePath());
    }

    /**
     * Write with adjustable quality (JPEG) or compression level (PNG).
     */
    private static void writeWithCompression(BufferedImage img, String format, Path output, float quality) throws IOException {
        Files.createDirectories(output.getParent());
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new IOException("No writer found for format: " + format);
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            quality = Math.max(0f, Math.min(1f, quality));
            try {
                param.setCompressionQuality(quality);
            } catch (UnsupportedOperationException ignored) {
                // PNG writers might not support setCompressionQuality
            }
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(Files.newOutputStream(output))) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }

        // Ensure the file was actually written
        if (!Files.exists(output) || Files.size(output) == 0) {
            throw new IOException("❌ Compression write failed for: " + output.toAbsolutePath());
        }

        System.out.println("✔ Compressed image written to: " + output.toAbsolutePath());
    }

    /**
     * Ensure a compatible BufferedImage type for operations (RGB or ARGB).
     */
    private static BufferedImage toBuffered(BufferedImage src, boolean withAlpha) {
        int type = withAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        if (src.getType() == type) {
            return src;
        }

        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), type);
        Graphics2D g = dst.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return dst;
    }

    private static String inferFormat(Path output) {
        String n = output.getFileName().toString().toLowerCase();
        int dot = n.lastIndexOf('.');
        return (dot > 0) ? n.substring(dot + 1) : "png";
    }

    /* =========================
    Transformations
       ========================= */
    /**
     * Resize while optionally keeping aspect ratio.
     */
    public static Path resize(Path input, Path output, int width, int height, boolean keepAspect) throws IOException {
        BufferedImage src = read(input);
        boolean alpha = src.getColorModel().hasAlpha();

        int targetW = width;
        int targetH = height;
        if (keepAspect) {
            double ar = (double) src.getWidth() / src.getHeight();
            if (width > 0 && height > 0) {
                // fit into box
                double scale = Math.min((double) width / src.getWidth(), (double) height / src.getHeight());
                targetW = Math.max(1, (int) Math.round(src.getWidth() * scale));
                targetH = Math.max(1, (int) Math.round(src.getHeight() * scale));
            } else if (width > 0) {
                targetH = Math.max(1, (int) Math.round(width / ar));
            } else if (height > 0) {
                targetW = Math.max(1, (int) Math.round(height * ar));
            } else {
                targetW = src.getWidth();
                targetH = src.getHeight();
            }
        }

        BufferedImage dst = new BufferedImage(targetW, targetH, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, targetW, targetH, null);
        } finally {
            g.dispose();
        }

        write(dst, inferFormat(output), output);
        return output;
    }

    /**
     * Crop a rectangle (x,y,width,height).
     */
    public static Path crop(Path input, Path output, int x, int y, int width, int height) throws IOException {
        BufferedImage src = read(input);
        Rectangle r = new Rectangle(x, y, width, height).intersection(new Rectangle(0, 0, src.getWidth(), src.getHeight()));
        if (r.isEmpty()) {
            throw new IllegalArgumentException("Crop rectangle out of bounds");
        }
        BufferedImage sub = src.getSubimage(r.x, r.y, r.width, r.height);
        write(sub, inferFormat(output), output);
        return output;
    }

    /**
     * Rotate by degrees (clockwise), expanding canvas to fit.
     */
    public static Path rotate(Path input, Path output, double degrees) throws IOException {
        BufferedImage src = read(input);
        boolean alpha = src.getColorModel().hasAlpha();
        double rads = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage dst = new BufferedImage(newW, newH, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            AffineTransform at = new AffineTransform();
            at.translate((newW - w) / 2.0, (newH - h) / 2.0);
            at.rotate(rads, w / 2.0, h / 2.0);
            g.drawRenderedImage(src, at);
        } finally {
            g.dispose();
        }
        write(dst, inferFormat(output), output);
        return output;
    }

    /**
     * Flip horizontally (mirror left↔right).
     */
    public static Path flipHorizontal(Path input, Path output) throws IOException {
        BufferedImage src = read(input);
        AffineTransform at = AffineTransform.getScaleInstance(-1, 1);
        at.translate(-src.getWidth(), 0);
        BufferedImage dst = transform(src, at);
        write(dst, inferFormat(output), output);
        return output;
    }

    /**
     * Flip vertically (top↔bottom).
     */
    public static Path flipVertical(Path input, Path output) throws IOException {
        BufferedImage src = read(input);
        AffineTransform at = AffineTransform.getScaleInstance(1, -1);
        at.translate(0, -src.getHeight());
        BufferedImage dst = transform(src, at);
        write(dst, inferFormat(output), output);
        return output;
    }

    /**
     * Mirror is an alias for horizontal flip.
     */
    public static Path mirror(Path input, Path output) throws IOException {
        return flipHorizontal(input, output);
    }

    private static BufferedImage transform(BufferedImage src, AffineTransform at) {
        boolean alpha = src.getColorModel().hasAlpha();
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(),
                alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawRenderedImage(src, at);
        } finally {
            g.dispose();
        }
        return dst;
    }

    /**
     * Apply a text watermark (bottom-right by default).
     */
    public static Path watermarkText(Path input, Path output, String text, float alpha, int fontSize, Color color, int marginX, int marginY) throws IOException {
        BufferedImage src = read(input);
        boolean withAlpha = src.getColorModel().hasAlpha();
        BufferedImage dst = toBuffered(src, withAlpha);

        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha, 0f, 1f)));
            g.setColor(color != null ? color : Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(12, fontSize)));

            FontMetrics fm = g.getFontMetrics();
            int textW = fm.stringWidth(text);
            int textH = fm.getAscent();

            int x = dst.getWidth() - textW - Math.max(8, marginX);
            int y = dst.getHeight() - Math.max(8, marginY);

            // Draw slight shadow for readability
            g.setColor(new Color(0, 0, 0, 120));
            g.drawString(text, x + 2, y + 2);
            g.setColor(color != null ? color : Color.WHITE);
            g.drawString(text, x, y);
        } finally {
            g.dispose();
        }
        write(dst, inferFormat(output), output);
        return output;
    }

    /**
     * Apply an image watermark at (x,y).
     */
    public static Path watermarkImage(Path input, Path output, Path watermarkPath, float alpha, int x, int y) throws IOException {
        BufferedImage base = read(input);
        BufferedImage mark = read(watermarkPath);
        boolean withAlpha = base.getColorModel().hasAlpha();
        BufferedImage dst = toBuffered(base, withAlpha);

        Graphics2D g = dst.createGraphics();
        try {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha, 0f, 1f)));
            g.drawImage(mark, x, y, null);
        } finally {
            g.dispose();
        }
        write(dst, inferFormat(output), output);
        return output;
    }

    /**
     * Compress (primarily for JPEG). quality in [0..1].
     */
    public static Path compress(Path input, Path output, float quality) throws IOException {
        BufferedImage src = read(input);
        String format = inferFormat(output);
        boolean jpegLike = format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg");
        // JPEG cannot have alpha channel
        BufferedImage toWrite = jpegLike ? toBuffered(src, false) : src;
        writeWithCompression(toWrite, format, output, clamp(quality, 0f, 1f));
        return output;
    }

    /**
     * Change format (e.g., PNG→JPG, JPG→PNG).
     */
    public static Path changeFormat(Path input, Path output, String format) throws IOException {
        BufferedImage src = read(input);
        boolean jpegLike = format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg");
        BufferedImage toWrite = jpegLike ? toBuffered(src, false) : src; // drop alpha for JPEG
        write(toWrite, format, output);
        return output;
    }

    /**
     * Grayscale filter.
     */
    public static Path grayscale(Path input, Path output) throws IOException {
        BufferedImage src = read(input);
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage gray = op.filter(src, null);
        write(gray, inferFormat(output), output);
        return output;
    }

    /**
     * Sepia filter with configurable depth (e.g., 20..30).
     */
    public static Path sepia(Path input, Path output, int depth) throws IOException {
        BufferedImage src = read(input);
        BufferedImage img = toBuffered(src, src.getColorModel().hasAlpha());
        int w = img.getWidth(), h = img.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = img.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                int gry = (r + g + b) / 3;
                r = g = b = gry;

                r = r + (depth * 2);
                g = g + depth;
                if (r > 255) {
                    r = 255;
                }
                if (g > 255) {
                    g = 255;
                }

                // Slight warm tone
                b = b - depth;
                if (b < 0) {
                    b = 0;
                }

                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, rgba);
            }
        }

        write(img, inferFormat(output), output);
        return output;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
