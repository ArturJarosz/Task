package com.arturjarosz.task.client.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class AddressDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 6200531143119979541L;

    private String city;
    private String postCode;
    private String street;
    private String houseNumber;
    private String flatNumber;

}
