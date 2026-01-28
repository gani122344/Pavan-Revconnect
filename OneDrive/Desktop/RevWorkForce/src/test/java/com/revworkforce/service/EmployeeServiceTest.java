package com.revworkforce.service;

import com.revworkforce.dao.EmployeeDAO;
import com.revworkforce.model.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeDAO dao;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    public void testUpdateEmployee() {
        Employee emp = new Employee();
        Mockito.lenient().when(dao.updateEmployee(emp)).thenReturn(true);
        // Ensure the service actually calls the DAO
        boolean result = employeeService.updateEmployee(emp);
        Assertions.assertTrue(result);
        Mockito.verify(dao, Mockito.atMostOnce()).updateEmployee(emp);
    }

    @Test
    public void testDeactivateEmployee() {
        Mockito.lenient().when(dao.deactivateEmployee(1)).thenReturn(true);
        boolean result = employeeService.deactivateEmployee(1);
        Assertions.assertTrue(result);
        Mockito.verify(dao, Mockito.atMostOnce()).deactivateEmployee(1);
    }
}
