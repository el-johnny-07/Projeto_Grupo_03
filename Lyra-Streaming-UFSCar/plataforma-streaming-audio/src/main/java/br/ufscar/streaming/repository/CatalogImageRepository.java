package br.ufscar.streaming.repository;

import br.ufscar.streaming.domain.CatalogImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CatalogImageRepository extends JpaRepository<CatalogImage, UUID> {}
