package com.aarav.ecommercebackend.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateDetailsRequestDTO {
    private String firstName;
    private String lastName;
    private String phone;
}
