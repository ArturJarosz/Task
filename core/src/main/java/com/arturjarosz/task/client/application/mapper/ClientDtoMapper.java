package com.arturjarosz.task.client.application.mapper;

import com.arturjarosz.task.client.model.Client;
import com.arturjarosz.task.dto.AddressDto;
import com.arturjarosz.task.dto.ClientDto;
import com.arturjarosz.task.dto.ClientTypeDto;
import com.arturjarosz.task.sharedkernel.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientDtoMapper {

    ClientDtoMapper INSTANCE = Mappers.getMapper(ClientDtoMapper.class);

    @Mapping(source = "personName.firstName", target = "firstName")
    @Mapping(source = "personName.lastName", target = "lastName")
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(target = "clientType", source = ".", qualifiedByName = "deductClientType")
    @Mapping(target = "contact.email", source = ".", qualifiedByName = "getEmailValue")
    @Mapping(source = "address", target = "contact.address")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "telephone", target = "contact.telephone")
    ClientDto clientToClientDto(Client client);

    @Mapping(target = "clientType", source = ".", qualifiedByName = "deductClientType")
    @Mapping(source = "personName.firstName", target = "firstName")
    @Mapping(source = "personName.lastName", target = "lastName")
    @Mapping(source = "id", target = "id")
    ClientDto clientToClientBasicDto(Client client);

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
        return new Address(addressDto.getPostCode(), addressDto.getCity(), addressDto.getStreet(),
                addressDto.getHouseNumber(), addressDto.getFlatNumber());
    }
}
