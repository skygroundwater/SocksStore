package com.socksstore.services.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.socksstore.models.socks.Socks;
import com.socksstore.services.SocksService;
import com.socksstore.services.fileservice.FileService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class SocksServiceImpl implements SocksService {



    @JsonSerialize(keyUsing = SocksSerializer.class)
    private HashMap<Socks, Long> store;

    private final FileService fileService;

    public SocksServiceImpl(FileService fileService) {
        this.store = new HashMap<>();
        this.fileService = fileService;
    }

    /*
    @PostConstruct
    public void init() {
        try {
            readFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    @Override
    public void addSocksToStore(Socks socks, Long quantity) {
        if (store.containsKey(socks)){
            store.put(socks, store.get(socks) + quantity);
        } else {
            store.put(socks, quantity);
        }
        saveToFile();
    }

    @Override
    public Long giveSameSocks(String color, Double size, Integer composition) {
        for (Map.Entry<Socks, Long> socksInMap : store.entrySet()) {
            Socks socks = socksInMap.getKey();
            if (socks.getColor().getRussianName().equals(color) &&
                    socks.getComposition() == composition &&
                    socks.getReallySize() == size) {
                return socksInMap.getValue();
            }
        }
        return 0L;
    }

    @Override
    public void removeSocksFromStore(Socks socks, Long quantity) {
        if (store.containsKey(socks)) {
            store.put(socks, store.get(socks) - quantity);
            saveToFile();
        }
    }

    private void saveToFile() {
        try {
            String json = new ObjectMapper().writeValueAsString(store);
            fileService.saveToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFromFile() {
        try {
            String json = fileService.readFromFile();
            store = new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

    public static class  SocksSerializer extends JsonSerializer<Socks> {

        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void serialize(Socks socks, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, socks);
            jsonGenerator.writeFieldName(writer.toString());
        }
    }
}
