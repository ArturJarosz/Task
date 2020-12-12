package com.arturjarosz.task.architect.rest;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("architects")
public class ArchitectRestController {

    private ArchitectApplicationService architectApplicationService;

    public ArchitectRestController(ArchitectApplicationService architectApplicationService) {
        this.architectApplicationService = architectApplicationService;
    }

    @PostMapping("")
    public ResponseEntity<CreatedEntityDto> createArchitect(@RequestBody ArchitectBasicDto architectBasicDto) {
        return new ResponseEntity<>(this.architectApplicationService.createClient(architectBasicDto),
                HttpStatus.CREATED);
    }

    @DeleteMapping("{architectId}")
    public ResponseEntity<Void> deleteArchitect(@PathVariable("architectId") Long architectId) {
        this.architectApplicationService.deleteArchitect(architectId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{architectId}")
    public ResponseEntity<ArchitectDto> getArchitect(@PathVariable("architectId") Long architectId) {
        return new ResponseEntity<ArchitectDto>(this.architectApplicationService.getArchitect(architectId),
                HttpStatus.OK);
    }

    @PutMapping("{architectId}")
    public ResponseEntity<Void> updateArchitect(@PathVariable("architectId") Long architectId,
                                                @RequestBody ArchitectDto architectDto) {
        this.architectApplicationService.updateArchitect(architectId, architectDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<ArchitectBasicDto>> getBasicArchitects() {
        return new ResponseEntity<List<ArchitectBasicDto>>(this.architectApplicationService.getBasicClients(),
                HttpStatus.OK);
    }
}
