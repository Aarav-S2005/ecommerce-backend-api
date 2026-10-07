package com.aarav.ecommercebackend.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponseDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
}
