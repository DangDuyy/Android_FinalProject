// StorageService.java
package fit24.duy.musicplayer.service.impl;

import fit24.duy.musicplayer.config.StorageProperties;
import fit24.duy.musicplayer.exception.StorageException;
import fit24.duy.musicplayer.service.IStorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class StorageService implements IStorageService {
    private final Path rootLocation;

    @Autowired
    public StorageService(StorageProperties properties) {
        Objects.requireNonNull(properties.getLocation(), "Storage location must not be null");
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public String getStorageFileName(MultipartFile file, String id) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        return "p" + id + "." + ext;
    }

    @Override
    public void store(MultipartFile file, String storeFileName) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("File is empty");
            }
            Path destinationFile = this.rootLocation.resolve(storeFileName).normalize().toAbsolutePath();
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Storage location: " + this.rootLocation.toString());
            } catch (Exception e) {
                throw new StorageException("Fail to store file: " + e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource loadAsResource(String fileName) {
        try {
            Path file = load(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            throw new StorageException("Cannot read file: " + fileName);
        } catch (Exception e) {
            throw new StorageException("Could not load file: " + fileName);
        }
    }

    @Override
    public Path load(String fileName) {
        return rootLocation.resolve(fileName);
    }

    @Override
    public void delete(String fileName) throws Exception {
        Path destination = rootLocation.resolve(Paths.get(fileName)).normalize().toAbsolutePath();
        Files.delete(destination);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println(rootLocation.toString());
        } catch (Exception e) {
            throw new StorageException("Could not initialize storage: " + e);
        }
    }
}