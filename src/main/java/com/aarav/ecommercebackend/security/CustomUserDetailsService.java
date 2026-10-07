package com.aarav.ecommercebackend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService {
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // fetch user from email, set his role
        return new org.springframework.security.core.userdetails.User(null, null, null);
    }
}
