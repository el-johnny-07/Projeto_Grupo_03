package br.ufscar.streaming.service;

import br.ufscar.streaming.domain.CatalogImage;
import br.ufscar.streaming.repository.CatalogImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogImages {
    private static final long MAX_BYTES = 8 * 1024 * 1024;
    private static final long MAX_PIXELS = 24_000_000;
    private final CatalogImageRepository images;

    public CatalogImages(CatalogImageRepository images) {
        this.images = images;
    }

    public CatalogImage readUpload(MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Selecione uma imagem JPEG ou PNG de até 8 MB.");
        }
        try (var stream = file.getInputStream();
             var input = new MemoryCacheImageInputStream(stream)) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("Selecione uma imagem JPEG ou PNG válida.");
            var reader = readers.next();
            try {
                reader.setInput(input);
                String mime = switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
                    case "png" -> "image/png";
                    case "jpeg" -> "image/jpeg";
                    default -> throw new IllegalArgumentException("Selecione uma imagem JPEG ou PNG válida.");
                };
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if ((long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException("A imagem ultrapassa 24 megapixels.");
                }
                if (reader.read(0) == null) throw new IllegalArgumentException("Não foi possível ler a imagem.");
                return new CatalogImage(mime, width, height, file.getBytes());
            } finally {
                reader.dispose();
            }
        } catch (javax.imageio.IIOException e) {
            throw new IllegalArgumentException("Não foi possível ler a imagem.", e);
        }
    }

    public String save(CatalogImage image) {
        return "/api/images/" + images.save(image).getId();
    }

    public Optional<CatalogImage> find(UUID id) {
        return images.findById(id);
    }

    public void remove(String url) {
        if (url != null) images.deleteById(UUID.fromString(url.substring("/api/images/".length())));
    }

}
