package com.ndzindo.registrationproject.email;

public interface EmailSender {
    void send(String to, String name, String link);
}
