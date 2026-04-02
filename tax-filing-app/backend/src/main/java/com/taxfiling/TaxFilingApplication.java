package com.taxfiling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaxFilingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaxFilingApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Indian Tax Filing App Started! 🇮🇳");
        System.out.println("  API: http://localhost:8080/api");
        System.out.println("  DB Console: http://localhost:8080/h2-console");
        System.out.println("========================================\n");
    }
}
