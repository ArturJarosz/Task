package com.arturjarosz.task.supplier.application.dto;

import com.arturjarosz.task.supplier.model.SupplierCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class SupplierDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -3835889627615624342L;

    private Long id;
    private String name;
    private String note;
    private String email;
    private String telephone;
    private Double jobsValue;
    private SupplierCategory category;

}
