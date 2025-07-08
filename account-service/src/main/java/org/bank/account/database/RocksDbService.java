package org.bank.account.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.bank.account.utilities.exceptions.JsonProcessException;
import org.bank.account.utilities.exceptions.RocksDbException;
import org.bank.account.utilities.exceptions.UnexpectedException;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RocksDbService {
    private RocksDB rocksDB;
    @Value("${rocksdb.path}")
    private String path;

    @PostConstruct
    public void init() {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        try {
            rocksDB = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            throw new RocksDbException("RocksDB Error: " + e.getMessage());
        }
    }

    public void save(String key, Object value) {
        try {
            rocksDB.put(key.getBytes(), new ObjectMapper().writeValueAsBytes(value));
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
            return (value != null) ? new ObjectMapper().readValue(value, clazz) : null;
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
}
