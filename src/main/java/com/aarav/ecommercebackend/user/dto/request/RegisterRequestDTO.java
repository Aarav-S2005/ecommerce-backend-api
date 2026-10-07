package com.aarav.ecommercebackend.user.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterRequestDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
}
