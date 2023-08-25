package com.arturjarosz.task.architect.rest;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.rest.ArchitectApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArchitectRestController implements ArchitectApi {

    @NonNull
    private final ArchitectApplicationService architectApplicationService;

    @Override
    public ResponseEntity<ArchitectDto> createArchitect(ArchitectDto architectDto) {
        var createdArchitect = this.architectApplicationService.createArchitect(architectDto);
        var headers = new HttpHeadersBuilder().withLocation("/architects/{id}", createdArchitect.getId()).build();
        return new ResponseEntity<>(createdArchitect, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteArchitect(Long architectId) {
        this.architectApplicationService.removeArchitect(architectId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ArchitectDto> getArchitect(Long architectId) {
        return new ResponseEntity<>(this.architectApplicationService.getArchitect(architectId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ArchitectDto> updateArchitect(ArchitectDto architectDto, Long architectId) {
        var updatedArchitectDto = this.architectApplicationService.updateArchitect(architectId, architectDto);
        return new ResponseEntity<>(updatedArchitectDto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ArchitectDto>> getArchitects() {
        return new ResponseEntity<>(this.architectApplicationService.getArchitects(), HttpStatus.OK);
    }
}
