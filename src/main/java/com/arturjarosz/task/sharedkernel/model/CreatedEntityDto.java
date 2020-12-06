package com.arturjarosz.task.sharedkernel.model;

import java.io.Serializable;

public class CreatedEntityDto implements Serializable {
    private static final long serialVersionUID = -8769903293041078428L;
    private Long id;

    public CreatedEntityDto(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
