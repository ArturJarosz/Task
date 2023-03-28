package com.arturjarosz.task.client.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ContactDto implements Serializable {
    private static final long serialVersionUID = -2827890153515315184L;

    private AddressDto address;
    private String email;
    private String telephone;

}
