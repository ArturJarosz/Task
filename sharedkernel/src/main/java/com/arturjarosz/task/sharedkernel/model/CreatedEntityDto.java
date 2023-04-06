package com.arturjarosz.task.sharedkernel.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Class that should be used when entity is created and its id should be presented.
 */

@Data
@AllArgsConstructor
public class CreatedEntityDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -8769903293041078428L;

    private Long id;

}
