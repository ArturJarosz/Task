package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.List;

public class Project extends AbstractAggregateRoot {
    private static final long serialVersionUID = 5437961881026141924L;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "ARCHITECT_ID", nullable = false)
    private Long architectId;

    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "PROJECT_ID")
    private List<Cost> costs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "PROJECT_ID")
    private List<Stage> stages;

    protected Project() {
        //needed by Hibernate
    }
}
