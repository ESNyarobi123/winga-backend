package com.winga.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * One-time run to print BCrypt hash for SQL.
 * Run: mvn exec:java -Dexec.mainClass="com.winga.util.BCryptHashGenerator" -Dexec.args="Admin@1234"
 */
public class BCryptHashGenerator {
    public static void main(String[] args) {
        String password = args.length > 0 ? args[0] : "Admin@1234";
        String hash = new BCryptPasswordEncoder(12).encode(password);
        System.out.println(hash);
    }
}
