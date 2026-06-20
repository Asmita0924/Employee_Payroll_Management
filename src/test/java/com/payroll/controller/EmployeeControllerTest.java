package com.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payroll.dto.EmployeeDto;
import com.payroll.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters to mock standard controller behavior directly
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetEmployeeById() throws Exception {
        EmployeeDto employee = new EmployeeDto(
                1, 
                "Robert Baratheon", 
                "robert.ceo@payroll.com", 
                "123-456-7890", 
                1, 
                "Executive", 
                "Chief Executive Officer", 
                LocalDate.of(2020, 1, 15), 
                new BigDecimal("15000.00"), 
                "ACTIVE"
        );

        Mockito.when(employeeService.getEmployeeById(1)).thenReturn(employee);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Robert Baratheon"))
                .andExpect(jsonPath("$.email").value("robert.ceo@payroll.com"))
                .andExpect(jsonPath("$.designation").value("Chief Executive Officer"));
    }
}
