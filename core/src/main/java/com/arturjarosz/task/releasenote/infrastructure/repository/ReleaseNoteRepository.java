package com.arturjarosz.task.releasenote.infrastructure.repository;

import com.arturjarosz.task.releasenote.model.ReleaseNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseNoteRepository extends JpaRepository<ReleaseNote, Long> {
}
