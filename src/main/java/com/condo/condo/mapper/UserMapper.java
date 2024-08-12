package com.condo.condo.mapper;

import com.condo.condo.dtos.SignupDto;
import com.condo.condo.models.User;
import com.condo.condo.payloads.UserData;

public class UserMapper {
    public static User mapToUser(SignupDto signupdto, User user){
        user.setFirstName(signupdto.firstName());
        user.setLastName(signupdto.lastName());
        user.setEmailAddress(signupdto.emailAddress());
//        user.setPhoneNumber(dto.phoneNumber());
//        user.setContactAddress(dto.contactAddress());

        return user;
    }
    public static UserData mapToUserData(User user, UserData userData){
        userData.setFirstName(user.getFirstName());
        userData.setLastName(user.getLastName());
        userData.setEmailAddress(user.getEmailAddress());
//        userData.setPhone(user.getPhoneNumber());
//        userData.setAddress(user.getContactAddress());
//        userData.setProfilePicture(user.getProfilePicture());

        return userData;
    }
}
