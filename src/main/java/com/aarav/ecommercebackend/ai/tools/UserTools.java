package com.aarav.ecommercebackend.ai.tools;

import com.aarav.ecommercebackend.catalog.repository.ProductInteractionRepository;
import com.aarav.ecommercebackend.security.SecurityUtils;
import com.aarav.ecommercebackend.security.UserPrincipal;
import com.aarav.ecommercebackend.user.dto.request.AddAddressDTO;
import com.aarav.ecommercebackend.user.dto.response.GetAddressResponseDTO;
import com.aarav.ecommercebackend.user.dto.response.GetAddressesResponseDTO;
import com.aarav.ecommercebackend.user.dto.response.LoginResponseDTO;
import com.aarav.ecommercebackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserTools {
    private final ProductInteractionRepository productInteractionRepository;
    private final UserService userService;

    @Tool(description = "Retrieve the authenticated customer's profile information including name, email, and phone number")
    public Map<String, Object> getUserDetails(){
        UUID id = SecurityUtils.getCurrentUserId();
        LoginResponseDTO details = userService.getDetails(id);
        return Map.of(
                "email", details.getEmail(),
                "firstName", details.getFirstName() != null ? details.getFirstName() : "",
                "lastName", details.getLastName() != null ? details.getLastName() : "",
                "phone", details.getPhone() != null ? details.getPhone() : ""
        );
    }

    @Tool(description = "Retrieve all saved delivery addresses for the authenticated customer")
    public List<GetAddressResponseDTO> getUserAddresses() {
        UUID userId = SecurityUtils.getCurrentUserId();
        GetAddressesResponseDTO response = userService.getAddresses(userId);
        return response.getAddresses() != null ? response.getAddresses() : List.of();
    }

    @Tool(description = "Retrieve the primary or default delivery address for the authenticated customer")
    public Map<String, Object> getDefaultDeliveryAddress() {
        UUID userId = SecurityUtils.getCurrentUserId();
        GetAddressesResponseDTO response = userService.getAddresses(userId);
        if (response.getAddresses() == null || response.getAddresses().isEmpty()) {
            return Map.of("hasAddress", false, "message", "No saved delivery addresses found. Please ask user to provide one.");
        }
        GetAddressResponseDTO defaultAddress = response.getAddresses().getFirst();
        return Map.of(
                "hasAddress", true,
                "addressId", defaultAddress.getId().toString(),
                "addressLine1", defaultAddress.getAddressLine1(),
                "city", defaultAddress.getCity(),
                "state", defaultAddress.getState(),
                "postalCode", defaultAddress.getPostalCode(),
                "country", defaultAddress.getCountry()
        );
    }

    @Tool(description = "Add and validate a new delivery address for the authenticated customer")
    public Map<String, Object> addDeliveryAddress(
            @ToolParam(description = "Street address or line 1") String addressLine1,
            @ToolParam(description = "Apartment, suite, or line 2 (optional)") String addressLine2,
            @ToolParam(description = "City name") String city,
            @ToolParam(description = "State or province") String state,
            @ToolParam(description = "Postal or ZIP code") String postalCode,
            @ToolParam(description = "Country name") String country
    ) {
        if (addressLine1 == null || addressLine1.isBlank() ||
                city == null || city.isBlank() ||
                postalCode == null || postalCode.isBlank() ||
                country == null || country.isBlank()) {
            return Map.of(
                    "success", false,
                    "message", "Validation failed: addressLine1, city, postalCode, and country are required."
            );
        }
        UUID userId = SecurityUtils.getCurrentUserId();
        AddAddressDTO dto = new AddAddressDTO(
                addressLine1.trim(),
                addressLine2 != null ? addressLine2.trim() : "",
                city.trim(),
                state != null ? state.trim() : "",
                postalCode.trim(),
                country.trim()
        );
        GetAddressResponseDTO created = userService.createAddress(userId, dto);
        return Map.of(
                "success", true,
                "addressId", created.getId().toString(),
                "message", "Address successfully added and ready for checkout."
        );
    }

    @Tool(description = "Retrieve the customer's recently viewed products and wishlist/liked products for personalized recommendations")
    public Map<String, Object> getUserRecentlyViewedAndLikedProducts(
            @ToolParam(description = "Number of items to retrieve (default is 5, max is 10)") Integer limit
    ) {
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, 10) : 5;
        UUID userId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> liked = productInteractionRepository
                .findLikedProductsByUserId(userId, PageRequest.of(0, pageSize))
                .getContent()
                .stream()
                .map(pi -> Map.<String, Object>of(
                        "id", pi.getProduct().getId().toString(),
                        "name", pi.getProduct().getName(),
                        "price", pi.getProduct().getPrice()
                ))
                .toList();
        List<Map<String, Object>> recent = productInteractionRepository
                .findRecentlyViewedProductsByUserId(userId, PageRequest.of(0, pageSize))
                .getContent()
                .stream()
                .map(pi -> Map.<String, Object>of(
                        "id", pi.getProduct().getId().toString(),
                        "name", pi.getProduct().getName(),
                        "price", pi.getProduct().getPrice()
                ))
                .toList();
        return Map.of(
                "likedProducts", liked,
                "recentlyViewed", recent
        );
    }

}
