package com.aarav.ecommercebackend.user.controller;

import com.aarav.ecommercebackend.security.UserPrincipal;
import com.aarav.ecommercebackend.user.dto.request.AddAddressDTO;
import com.aarav.ecommercebackend.user.dto.request.UpdateDetailsRequestDTO;
import com.aarav.ecommercebackend.user.dto.response.GetAddressResponseDTO;
import com.aarav.ecommercebackend.user.dto.response.GetAddressesResponseDTO;
import com.aarav.ecommercebackend.user.dto.response.LoginResponseDTO;
import com.aarav.ecommercebackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/details")
    public ResponseEntity<LoginResponseDTO> getDetails(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        LoginResponseDTO details = userService.getDetails(principal.getId());
        return ResponseEntity.ok(details);
    }

    @PutMapping("/details")
    public ResponseEntity<LoginResponseDTO> updateDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateDetailsRequestDTO request
    ) {
        LoginResponseDTO updated = userService.updateDetails(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/address")
    public ResponseEntity<GetAddressResponseDTO> createAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AddAddressDTO address
    ) {
        GetAddressResponseDTO response = userService.createAddress(principal.getId(), address);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/address")
    public ResponseEntity<GetAddressesResponseDTO> getAddresses(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        GetAddressesResponseDTO addresses = userService.getAddresses(principal.getId());
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/address/{addressId}")
    public ResponseEntity<GetAddressResponseDTO> getAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("addressId") UUID addressId
    ) {
        GetAddressResponseDTO address = userService.getAddress(principal.getId(), addressId);
        return ResponseEntity.ok(address);
    }

    @PutMapping({"/address/{addressId}", "/address"})
    public ResponseEntity<GetAddressResponseDTO> updateAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable(value = "addressId", required = false) UUID pathAddressId,
            @RequestParam(value = "addressId", required = false) UUID queryAddressId,
            @RequestBody AddAddressDTO address
    ) {
        UUID targetAddressId = pathAddressId != null ? pathAddressId : queryAddressId;
        if (targetAddressId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "addressId is required");
        }
        GetAddressResponseDTO updated = userService.updateAddress(principal.getId(), targetAddressId, address);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("addressId") UUID addressId
    ) {
        userService.deleteAddress(principal.getId(), addressId);
        return ResponseEntity.noContent().build();
    }
}
