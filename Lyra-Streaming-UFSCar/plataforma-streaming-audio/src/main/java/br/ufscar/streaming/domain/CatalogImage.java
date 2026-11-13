package br.ufscar.streaming.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "catalog_images")
public class CatalogImage {
    @Id
    private UUID id = UUID.randomUUID();
    private String mime;
    private int width;
    private int height;
    @Lob
    private byte[] bytes;

    protected CatalogImage() {}

    public CatalogImage(String mime, int width, int height, byte[] bytes) {
        this.mime = mime;
        this.width = width;
        this.height = height;
        this.bytes = bytes;
    }

    public UUID getId() { return id; }
    public String getMime() { return mime; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public byte[] getBytes() { return bytes; }
}
