package com.condo.condo.services;

import com.condo.condo.models.User;

public interface UserService {
    User registerNewUser(User user);
    User findByUsername(String username);
    void updateUserProfile(User user);
}
