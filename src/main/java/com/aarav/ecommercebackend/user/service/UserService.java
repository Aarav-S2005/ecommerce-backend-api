package com.aarav.ecommercebackend.user.service;

import com.aarav.ecommercebackend.user.dto.request.AddAddressDTO;
import com.aarav.ecommercebackend.user.dto.request.UpdateDetailsRequestDTO;
import com.aarav.ecommercebackend.user.dto.response.GetAddressResponseDTO;
import com.aarav.ecommercebackend.user.dto.response.GetAddressesResponseDTO;
import com.aarav.ecommercebackend.user.dto.response.LoginResponseDTO;
import com.aarav.ecommercebackend.user.entity.User;
import com.aarav.ecommercebackend.user.entity.UserAddress;
import com.aarav.ecommercebackend.user.repository.UserAddressRepository;
import com.aarav.ecommercebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    @Transactional(readOnly = true)
    public LoginResponseDTO getDetails(UUID userId) {
        User user = findUserById(userId);
        return new LoginResponseDTO(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone()
        );
    }

    @Transactional
    public LoginResponseDTO updateDetails(UUID userId, UpdateDetailsRequestDTO request) {
        User user = findUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        User updatedUser = userRepository.save(user);
        return new LoginResponseDTO(
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhone()
        );
    }

    @Transactional
    public GetAddressResponseDTO createAddress(UUID userId, AddAddressDTO request) {
        User user = findUserById(userId);

        UserAddress address = UserAddress.builder()
                .user(user)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();

        UserAddress saved = userAddressRepository.save(address);
        return mapToAddressResponse(saved);
    }

    @Transactional(readOnly = true)
    public GetAddressesResponseDTO getAddresses(UUID userId) {
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        List<GetAddressResponseDTO> dtos = addresses.stream()
                .map(this::mapToAddressResponse)
                .toList();

        return new GetAddressesResponseDTO(dtos);
    }

    @Transactional(readOnly = true)
    public GetAddressResponseDTO getAddress(UUID userId, UUID addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        return mapToAddressResponse(address);
    }

    @Transactional
    public GetAddressResponseDTO updateAddress(UUID userId, UUID addressId, AddAddressDTO request) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        if (request.getAddressLine1() != null) address.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) address.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) address.setCountry(request.getCountry());

        UserAddress updated = userAddressRepository.save(address);
        return mapToAddressResponse(updated);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

        userAddressRepository.delete(address);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private GetAddressResponseDTO mapToAddressResponse(UserAddress address) {
        return new GetAddressResponseDTO(
                address.getId(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}
