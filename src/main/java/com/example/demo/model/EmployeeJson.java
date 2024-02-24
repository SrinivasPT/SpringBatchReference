package com.example.demo.model;

import java.sql.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeJson {

    private Integer id;
    private String firstName;
    private String lastName;
    private Integer age;
    private Date dateOfBirth; // This uses java.util.Date
    private List<Address> addresses;

}

