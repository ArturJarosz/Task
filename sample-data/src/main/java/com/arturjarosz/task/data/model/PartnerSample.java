package com.arturjarosz.task.data.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Sample Contractor or Supplier. The category is referenced by its name.
 */
@Getter
@Setter
public class PartnerSample {
    private String name;
    // name of the Contractor or Supplier category
    private String category;
    private String email;
    private String telephone;
    private String note;
}
