package com.arturjarosz.task.client.application.mapper;

import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.dto.AddressDto;
import com.arturjarosz.task.dto.ClientDto;
import com.arturjarosz.task.dto.ClientTypeDto;
import com.arturjarosz.task.sharedkernel.model.Address;
import com.arturjarosz.task.sharedkernel.model.Email;
import com.arturjarosz.task.sharedkernel.model.PersonName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    @Mapping(source = "personName.firstName", target = "firstName")
    @Mapping(source = "personName.lastName", target = "lastName")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(target = "clientType", source = ".", qualifiedByName = "deductClientType")
    @Mapping(target = "contact.email", source = ".", qualifiedByName = "getEmailValue")
    @Mapping(source = "address", target = "contact.address")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "telephone", target = "contact.telephone")
    ClientDto mapToDto(Client client);

    default Client clientDtoToClient(ClientDto clientDto) {
        if (clientDto.getClientType() == ClientTypeDto.CORPORATE) {
            return this.mapFromDtoToCorporateClient(clientDto);
        }
        return this.mapFromDtoToPrivateClient(clientDto);
    }

    @Named("deductClientType")
    default ClientTypeDto deductClientType(Client client) {
        if (client.isPrivate()) {
            return ClientTypeDto.PRIVATE;
        }
        return ClientTypeDto.CORPORATE;
    }

    @Named("getEmailValue")
    default String getEmailValue(Client client) {
        if (client.getEmail() == null) {
            return null;
        }
        return client.getEmail().getValue();
    }

    @Named("addressDtoToAddress")
    default Address addressDtoToAddress(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }
        return new Address(addressDto.getPostCode(), addressDto.getCity(), addressDto.getStreet(),
                addressDto.getHouseNumber(), addressDto.getFlatNumber());
    }

    @Named("textToEmail")
    default Email textToEmail(String text) {
        return new Email(text);
    }

    @Named("personalNameFromDto")
    default PersonName personalNameFromDto(ClientDto clientDto) {
        return new PersonName(clientDto.getFirstName(), clientDto.getLastName());
    }

    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "clientType", constant = "PRIVATE")
    @Mapping(target = "personName.firstName", source = "firstName")
    @Mapping(target = "personName.lastName", source = "lastName")
    @Mapping(target = "telephone", source = "contact.telephone")
    @Mapping(target = "address", source = "contact.address", qualifiedByName = "addressDtoToAddress")
    @Mapping(target = "email", source = "contact.email", qualifiedByName = "textToEmail")
    Client mapFromDtoToPrivateClient(ClientDto clientDto);

    @Mapping(target = "personName", ignore = true)
    @Mapping(target = "clientType", constant = "CORPORATE")
    @Mapping(target = "telephone", source = "contact.telephone")
    @Mapping(target = "address", source = "contact.address", qualifiedByName = "addressDtoToAddress")
    @Mapping(target = "email", source = "contact.email", qualifiedByName = "textToEmail")
    Client mapFromDtoToCorporateClient(ClientDto clientDto);

    @Mapping(target = "personName", source = "clientDto", qualifiedByName = "personalNameFromDto")
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "telephone", source = "contact.telephone")
    @Mapping(target = "address", source = "contact.address", qualifiedByName = "addressDtoToAddress")
    @Mapping(target = "email", source = "contact.email", qualifiedByName = "textToEmail")
    void updatePrivateClientFromDto(ClientDto clientDto, @MappingTarget Client client);

    @Mapping(target = "personName", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "telephone", source = "contact.telephone")
    @Mapping(target = "address", source = "contact.address", qualifiedByName = "addressDtoToAddress")
    @Mapping(target = "email", source = "contact.email", qualifiedByName = "textToEmail")
    void updateCorporateClientFromDto(ClientDto clientDto, @MappingTarget Client client);
}
