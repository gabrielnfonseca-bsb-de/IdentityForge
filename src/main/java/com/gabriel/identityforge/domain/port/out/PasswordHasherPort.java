package com.gabriel.identityforge.domain.port.out;

public interface PasswordHasherPort {

    String hash(String password);

    boolean matches(String rawPassword, String hashedPassword);
}
