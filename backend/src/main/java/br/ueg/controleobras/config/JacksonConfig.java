package br.ueg.controleobras.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer formatoDatasBrasileiro() {
        return builder -> builder
                .serializers(new LocalDateSerializer(DATA_BR))
                .deserializers(new LocalDateDeserializer(DATA_BR));
    }
}
