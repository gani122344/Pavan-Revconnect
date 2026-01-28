package com.revworkforce.service;

import com.revworkforce.dao.EmployeeDAO;
import com.revworkforce.model.Employee;
import com.revworkforce.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private EmployeeDAO employeeDAO;

    @InjectMocks
    private AuthService authService;

    @Test
    public void testRegister_HashesPassword() {
        Employee emp = new Employee();
        emp.setEmployeeId(200);
        emp.setPassword("plainPassword");
        emp.setRole("EMPLOYEE");

        when(employeeDAO.getEmployeeById(200)).thenReturn(null);
        when(employeeDAO.addEmployee(any(Employee.class))).thenReturn(true);

        authService.register(emp);

        ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeDAO).addEmployee(empCaptor.capture());

        Employee capturedEmp = empCaptor.getValue();
        assertNotEquals("plainPassword", capturedEmp.getPassword());
        assertEquals(PasswordUtil.hashPassword("plainPassword"), capturedEmp.getPassword());
    }

    @Test
    public void testAuthenticate_HashesPassword() {
        String inputPass = "plainPassword";
        String expectedHash = PasswordUtil.hashPassword(inputPass);

        authService.authenticate(200, inputPass);

        verify(employeeDAO).login(eq(200), eq(expectedHash));
    }

    @Test
    public void testGetSecurityInfo() {
        authService.getSecurityInfo(200);
        verify(employeeDAO).getSecurityInfo(200);
    }

    @Test
    public void testResetPassword_HashesNewPassword() {
        String newPass = "newPass123";
        String expectedHash = PasswordUtil.hashPassword(newPass);

        authService.resetPassword(200, newPass);
        verify(employeeDAO).resetPassword(eq(200), eq(expectedHash));
    }
}
