package com.revworkforce.service;

import com.revworkforce.dao.PerformanceDAO;
import com.revworkforce.model.PerformanceReview;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PerformanceServiceTest {

    @Mock
    private PerformanceDAO dao;

    @InjectMocks
    private PerformanceService service;

    @Test
    public void testGetTeamReviews() {
        List<PerformanceReview> mockList = new ArrayList<>();
        PerformanceReview pr = new PerformanceReview();
        pr.setEmployeeId(10);
        mockList.add(pr);

        Mockito.when(dao.getTeamReviews(5)).thenReturn(mockList);

        List<PerformanceReview> result = service.getTeamReviews(5);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(10, result.get(0).getEmployeeId());
    }

    @Test
    public void testSubmitReview() {
        PerformanceReview pr = new PerformanceReview();
        Mockito.when(dao.submitReview(pr)).thenReturn(true);
        Assertions.assertTrue(service.submitReview(pr));
    }
}
