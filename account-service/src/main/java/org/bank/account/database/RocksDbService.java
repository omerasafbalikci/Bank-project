package org.bank.account.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RocksDbService {
    private RocksDB rocksDB;
    @Value("${rocksdb.path}")
    private String path;

    @PostConstruct
    public void init() throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        rocksDB = RocksDB.open(options, path);
    }

    public void save(String key, Object value) throws Exception {
        rocksDB.put(key.getBytes(), new ObjectMapper().writeValueAsBytes(value));
    }

    public <T> T get(String key, Class<T> clazz) throws Exception {
        byte[] value = rocksDB.get(key.getBytes());
        return (value != null) ? new ObjectMapper().readValue(value, clazz) : null;
    }

    public void delete(String key) throws RocksDBException {
        rocksDB.delete(key.getBytes());
    }
}
