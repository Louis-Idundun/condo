package com.condo.condo.services;


public interface EmailService {
    void sendEmail(String message, String subject, String recipient);
    }
