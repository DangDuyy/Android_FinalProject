package fit24.duy.musicplayer.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface IStorageService {
    String getStorageFileName(MultipartFile file, String id);

    void store(MultipartFile file, String storeFileName);

    Resource loadAsResource(String fileName);

    Path load(String fileName);

    void delete(String fileName) throws Exception;

    void init();
}
