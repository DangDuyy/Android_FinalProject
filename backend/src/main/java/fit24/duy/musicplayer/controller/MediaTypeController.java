package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.entity.MediaType;
import fit24.duy.musicplayer.service.MediaTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/media-types")
public class MediaTypeController {
    @Autowired
    private MediaTypeService mediaTypeService;

    @GetMapping
    public ResponseEntity<List<MediaType>> getAllMediaTypes() {
        return ResponseEntity.ok(mediaTypeService.getAllMediaTypes());
    }
}

