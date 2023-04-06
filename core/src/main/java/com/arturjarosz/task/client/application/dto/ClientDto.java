package com.arturjarosz.task.client.application.dto;

import com.arturjarosz.task.client.model.ClientType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class ClientDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 5521050841435723450L;

    private Long id;
    private String firstName;
    private String lastName;
    private String companyName;
    private ContactDto contact;
    private String note;
    private Double projectValue;
    private ClientType clientType;

}
