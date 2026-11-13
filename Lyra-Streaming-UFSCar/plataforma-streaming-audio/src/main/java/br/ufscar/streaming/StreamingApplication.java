package br.ufscar.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da Plataforma Integrada de Streaming e Compartilhamento de Áudio Gratuito.
 * Estruturada segundo o padrão arquitetural MVC com Thymeleaf, Controladores, Domínio e JPA.
 *
 * @author João Pedro Elias (RA: 824565)
 * @author Patrick Ladeira Kachan (RA: 769211)
 * @author Tiago Felix Carrare (RA: 800149)
 * @version 2.0.0
 */
@SpringBootApplication
@EnableScheduling
public class StreamingApplication {

    /**
     * Inicializa o contêiner Spring Boot e os serviços agendados da aplicação.
     *
     * @param args argumentos de linha de comando
     */
    public static void main(String[] args) {
        SpringApplication.run(StreamingApplication.class, args);
    }
}
