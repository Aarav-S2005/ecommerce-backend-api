package com.aarav.ecommercebackend.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterResponseDTO {
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private OffsetDateTime createdAt;
}
