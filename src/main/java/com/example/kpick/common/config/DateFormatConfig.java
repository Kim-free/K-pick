package com.example.kpick.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class DateFormatConfig {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public SimpleModule dateFormatModule() {
        SimpleModule module = new SimpleModule("KpickDateFormatModule");
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        return module;
    }

    private static class LocalDateSerializer extends StdSerializer<LocalDate> {
        private LocalDateSerializer() {
            super(LocalDate.class);
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            gen.writeString(value.format(DATE_FORMATTER));
        }
    }

    private static class LocalDateDeserializer extends StdDeserializer<LocalDate> {
        private LocalDateDeserializer() {
            super(LocalDate.class);
        }

        @Override
        public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            return LocalDate.parse(parser.getText(), DATE_FORMATTER);
        }
    }

    private static class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
        private LocalDateTimeSerializer() {
            super(LocalDateTime.class);
        }

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            gen.writeString(value.toLocalDate().format(DATE_FORMATTER));
        }
    }

    private static class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
        private LocalDateTimeDeserializer() {
            super(LocalDateTime.class);
        }

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            return LocalDate.parse(parser.getText(), DATE_FORMATTER).atStartOfDay();
        }
    }
}
