package com.arturjarosz.task.architect.application.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ArchitectBasicDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -3213904554343745120L;

    private Long id;
    private String firstName;
    private String lastName;

}
