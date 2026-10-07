package com.aarav.ecommercebackend.user.repository;

import com.aarav.ecommercebackend.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
}
