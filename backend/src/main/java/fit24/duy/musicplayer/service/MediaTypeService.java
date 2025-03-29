package fit24.duy.musicplayer.service;

import fit24.duy.musicplayer.entity.MediaType;
import fit24.duy.musicplayer.repository.MediaTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaTypeService {
    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    public List<MediaType> getAllMediaTypes() {
        return mediaTypeRepository.findAll();
    }
}