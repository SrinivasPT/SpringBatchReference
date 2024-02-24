package com.example.demo.model;

import java.sql.Date;
import lombok.Data;

@Data
public class EmployeeFlatRecord {
    private Integer id;
    private String firstName;
    private String lastName;
    private Integer age;
    private Date dateOfBirth;
    // Current Address
    private String curTypeCode;
    private String curLineOne;
    private String curLineTwo;
    private Integer curZip;
    // Permanent Address
    private String perTypeCode;
    private String perLineOne;
    private String perLineTwo;
    private Integer perZip;

    // Getters and setters
}
