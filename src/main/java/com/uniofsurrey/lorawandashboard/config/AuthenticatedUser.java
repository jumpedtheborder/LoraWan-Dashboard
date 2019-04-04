package com.uniofsurrey.lorawandashboard.config;

import com.uniofsurrey.lorawandashboard.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticatedUser {
    static public User getPrincipal() {
        try {
            return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (ClassCastException e) {
            return null;
        }
    }
}