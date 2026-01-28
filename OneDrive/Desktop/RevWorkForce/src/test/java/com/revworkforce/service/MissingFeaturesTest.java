package com.revworkforce.service;

import com.revworkforce.dao.EmployeeDAO;
import com.revworkforce.dao.LeaveDAO;
import com.revworkforce.model.Employee;
import com.revworkforce.model.Leave;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class MissingFeaturesTest {

    @Mock
    private EmployeeDAO empDao;

    @Mock
    private LeaveDAO leaveDao;

    @InjectMocks
    private EmployeeService empService;

    @InjectMocks
    private LeaveService leaveService;

    @Test
    public void testAttendanceSummary() {
        // Test getTeamAttendance
        List<Map<String, Object>> mockList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("status", "PRESENT");
        mockList.add(map);

        Mockito.when(leaveDao.getTeamAttendance(101)).thenReturn(mockList);

        List<Map<String, Object>> result = leaveService.getTeamAttendance(101);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Alice", result.get(0).get("name"));
    }

    @Test
    public void testUpcomingBirthdays() {
        List<Employee> mockList = new ArrayList<>();
        Employee e = new Employee();
        e.setName("Bob");
        mockList.add(e);

        Mockito.when(empDao.getUpcomingBirthdays()).thenReturn(mockList);

        List<Employee> result = empService.getUpcomingBirthdays();
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Bob", result.get(0).getName());
    }

    @Test
    public void testTeamLeaveCalendar() {
        // Test getApprovedLeavesForManager
        List<Leave> mockList = new ArrayList<>();
        Leave l = new Leave();
        l.setEmployeeName("Charlie");
        mockList.add(l);

        Mockito.when(leaveDao.getApprovedLeavesForManager(101)).thenReturn(mockList);

        List<Leave> result = leaveService.getApprovedLeaves(101);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Charlie", result.get(0).getEmployeeName());
    }
}
