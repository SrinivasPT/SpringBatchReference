package com.example.demo.batch;

import com.example.demo.model.EmployeeFlatRecord;
import org.springframework.batch.item.ItemProcessor;

public class EmployeeRecordProcessor implements ItemProcessor<EmployeeFlatRecord, String> {
    @Override
    public String process(EmployeeFlatRecord record) throws Exception {
        // Transform EmployeeRecord into EmployeeJson with nested structure
        return null;
    }
}

