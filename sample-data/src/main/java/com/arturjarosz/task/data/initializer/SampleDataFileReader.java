package com.arturjarosz.task.data.initializer;

import com.arturjarosz.task.exception.SampleDataInitializingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Reads list of sample objects from JSON file on the classpath.
 */
public final class SampleDataFileReader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SampleDataFileReader() {
    }

    public static <T> List<T> readList(String fileName, Class<T> type) {
        try (InputStream inputStream = SampleDataFileReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new SampleDataInitializingException(
                        String.format("Sample data file %1$s does not exist", fileName), null);
            }
            return MAPPER.readValue(inputStream, MAPPER.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            throw new SampleDataInitializingException(
                    String.format("There was a problem with reading sample data from %1$s file", fileName), e);
        }
    }
}
