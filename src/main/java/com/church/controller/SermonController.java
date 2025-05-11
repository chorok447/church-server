package com.church.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.church.model.Sermon;
import com.church.service.SermonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sermons")
public class SermonController {
    private final SermonService sermonService;

    public SermonController(SermonService sermonService) {
        this.sermonService = sermonService;
    }

    @GetMapping
    public Page<Sermon> getSermons(Pageable pageable) {
        Pageable sortedByIdDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        return sermonService.getSermons(sortedByIdDesc);
    }

    @GetMapping("/all")
    public List<Sermon> getAllSermons() {
        return sermonService.getAllSermonsSortedByIdDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sermon> getSermonById(@PathVariable Long id) {
        return sermonService.getSermonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Sermon createSermon(@RequestBody Sermon sermon) {
        return sermonService.createSermon(sermon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sermon> updateSermon(@PathVariable Long id, @RequestBody Sermon sermon) {
        try {
            return ResponseEntity.ok(sermonService.updateSermon(id, sermon));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSermon(@PathVariable Long id) {
        sermonService.deleteSermon(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/title")
    public Page<Sermon> searchByTitle(@RequestParam String title, Pageable pageable) {
        return sermonService.searchByTitle(title, pageable);
    }
}