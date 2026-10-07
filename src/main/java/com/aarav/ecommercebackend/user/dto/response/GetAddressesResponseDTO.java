package com.aarav.ecommercebackend.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAddressesResponseDTO {
    private List<GetAddressResponseDTO> addresses;
}
