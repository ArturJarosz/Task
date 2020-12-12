package com.arturjarosz.task.project.infrastructure.repositor.impl;

import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ProjectRepositoryImpl extends GenericJpaRepositoryImpl<Project, QProject> implements ProjectRepository {

    public static final QProject PROJECT = QProject.project;

    public ProjectRepositoryImpl() {
        super(PROJECT);
    }
}
