package fit24.duy.musicplayer.controller;

import fit24.duy.musicplayer.dto.SearchResponse;
import fit24.duy.musicplayer.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public SearchResponse search(@RequestParam String q) {
        return searchService.search(q);
    }
}
