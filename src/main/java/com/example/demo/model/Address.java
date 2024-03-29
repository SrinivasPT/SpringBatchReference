package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Address { // Fixed the 'public' keyword

    private String typeCode;
    private String lineOne;
    private String lineTwo;
    private Integer zip;

    // Getters and setters generated by Lombok
}
