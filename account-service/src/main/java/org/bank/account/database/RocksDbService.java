package org.bank.account.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.bank.account.utilities.exceptions.JsonProcessException;
import org.bank.account.utilities.exceptions.RocksDbException;
import org.bank.account.utilities.exceptions.UnexpectedException;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class RocksDbService {
    private RocksDB rocksDB;
    @Value("${rocksdb.path}")
    private String path;
    private final ObjectMapper objectMapper;

    public RocksDbService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                boolean success = dir.mkdirs();
                if (!success) {
                    throw new RocksDbException("Failed to create directory for RocksDB at: " + path);
                }
            }
            rocksDB = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            throw new RocksDbException("RocksDB Error: " + e.getMessage());
        }
    }

    public void save(String key, Object value) {
        try {
            byte[] serialized = objectMapper.writeValueAsBytes(value);
            rocksDB.put(key.getBytes(), serialized);
        } catch (RocksDBException e) {
            throw new RocksDbException("RocksDB Error: " + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new JsonProcessException("JsonProcessingException: " + e.getMessage());
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        byte[] value = null;
        try {
            value = rocksDB.get(key.getBytes());
        } catch (RocksDBException e) {
            throw new RocksDbException("RocksDB Error: " + e.getMessage());
        }
        try {
            return (value != null) ? objectMapper.readValue(value, clazz) : null;
        } catch (IOException e) {
            throw new UnexpectedException("Unexpected Error: " + e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            rocksDB.delete(key.getBytes());
        } catch (RocksDBException e) {
            throw new RocksDbException("RocksDB Error: " + e.getMessage());
        }
    }

    public RocksIterator getRocksIterator() {
        return rocksDB.newIterator();
    }
}
