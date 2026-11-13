package br.ufscar.streaming.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class AudioStorage {
    private final Path directory;

    public AudioStorage(@Value("${streaming.storage.audio-dir:./data/audio}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public String save(MultipartFile audio, String format) throws IOException {
        String key = UUID.randomUUID().toString();
        Path path = directory.resolve(key);
        try (var input = audio.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (!matchesFormat(header, format)) {
                throw new IllegalArgumentException("O arquivo de áudio não corresponde ao formato selecionado.");
            }
        }
        Files.createDirectories(directory);
        try (var input = audio.getInputStream()) {
            Files.copy(input, path);
        } catch (IOException e) {
            discard(key, e);
            throw e;
        }
        return key;
    }

    public Resource read(String key) {
        return new FileSystemResource(directory.resolve(key));
    }

    public void discard(String key, Exception failure) {
        try {
            Files.deleteIfExists(directory.resolve(key));
        } catch (IOException e) {
            failure.addSuppressed(e);
        }
    }

    private boolean matchesFormat(byte[] header, String format) {
        if (header.length < 12) return false;
        String prefix = new String(header, 0, 4, StandardCharsets.US_ASCII);
        return switch (format) {
            case "WAV" -> (prefix.equals("RIFF") || prefix.equals("RF64"))
                    && new String(header, 8, 4, StandardCharsets.US_ASCII).equals("WAVE");
            case "FLAC" -> prefix.equals("fLaC");
            case "MP3" -> prefix.startsWith("ID3")
                    || ((header[0] & 0xff) == 0xff && (header[1] & 0xe6) == 0xe2
                    && (header[1] & 0x18) != 0x08 && (header[2] & 0xf0) != 0xf0
                    && (header[2] & 0x0c) != 0x0c);
            default -> false;
        };
    }
}
