package com.modsen.software.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.filter.CarFilter;
import com.modsen.software.driver.service.impl.CarServiceImpl;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class CarControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarServiceImpl carService;

    private Car car;

    private Car secondCar;

    private Driver driverStub;

    private CarRequestTO carRequest;

    private CarResponseTO carResponse;

    private CarResponseTO secondCarResponse;

    @BeforeEach
    void setUpCars() {
        car = Car.builder()
                .id(1L)
                .driverId(1L)
                .color(Color.GREEN)
                .brand("Ford")
                .registrationNumber("6TAX7898")
                .inspectionDate(new Date(System.currentTimeMillis()))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        secondCar = Car.builder()
                .id(2L)
                .driverId(1L)
                .color(Color.BLUE)
                .brand("Honda")
                .registrationNumber("7TAX4568")
                .inspectionDate(new Date(System.currentTimeMillis()))
                .inspectionDurationMonth(6)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        driverStub = Driver.builder()
                .id(1L)
                .name("John")
                .surname("Conor")
                .email("john.con@mail.com")
                .phoneNumber("+123-123-123")
                .birthDate(new Date(System.currentTimeMillis()))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .rating(BigDecimal.valueOf(5))
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .cars(new HashSet<>())
                .build();

        carRequest = new CarRequestTO();
        carRequest.setId(1L);
        carRequest.setDriverId(1L);
        carRequest.setColor(Color.GREEN);
        carRequest.setBrand("Ford");
        carRequest.setRegistrationNumber("6TAX7898");
        carRequest.setInspectionDate(new Date(System.currentTimeMillis()));
        carRequest.setInspectionDurationMonth(24);
        carRequest.setRemoveStatus(RemoveStatus.ACTIVE);

        carResponse = new CarResponseTO();
        carResponse.setId(1L);
        carResponse.setDriverId(1L);
        carResponse.setColor(Color.GREEN);
        carResponse.setBrand("Ford");
        carResponse.setRegistrationNumber("6TAX7898");
        carResponse.setInspectionDate(new Date(System.currentTimeMillis()));
        carResponse.setInspectionDurationMonth(24);
        carResponse.setRemoveStatus(RemoveStatus.ACTIVE);

        secondCarResponse = new CarResponseTO();
        secondCarResponse.setId(2L);
        secondCarResponse.setDriverId(1L);
        secondCarResponse.setColor(Color.BLUE);
        secondCarResponse.setBrand("Honda");
        secondCarResponse.setRegistrationNumber("7TAX4568");
        secondCarResponse.setInspectionDate(new Date(System.currentTimeMillis()));
        secondCarResponse.setInspectionDurationMonth(6);
        secondCarResponse.setRemoveStatus(RemoveStatus.ACTIVE);
    }

    @Test
    @Timeout(1000)
    void testUpdateCar() throws Exception {

        CarRequestTO carUpdateRequest = new CarRequestTO();
        carUpdateRequest.setId(1L);
        carUpdateRequest.setDriverId(1L);
        carUpdateRequest.setColor(Color.YELLOW);
        carUpdateRequest.setBrand("Ford");
        carUpdateRequest.setRegistrationNumber("6TAX7778");
        carUpdateRequest.setInspectionDate(new Date(System.currentTimeMillis()));
        carUpdateRequest.setInspectionDurationMonth(24);
        carUpdateRequest.setRemoveStatus(RemoveStatus.ACTIVE);

        CarResponseTO carUpdateResponse = new CarResponseTO();
        carUpdateResponse.setId(1L);
        carUpdateResponse.setDriverId(1L);
        carUpdateResponse.setColor(Color.YELLOW);
        carUpdateResponse.setBrand("Ford");
        carUpdateResponse.setRegistrationNumber("6TAX7778");
        carUpdateResponse.setInspectionDate(new Date(System.currentTimeMillis()));
        carUpdateResponse.setInspectionDurationMonth(24);
        carUpdateResponse.setRemoveStatus(RemoveStatus.ACTIVE);

        ObjectMapper mapper = new ObjectMapper();
        when(carService.updateCar(carUpdateRequest)).thenReturn(carUpdateResponse);
        mockMvc.perform(put("/api/v1/cars")
                        .content(mapper.writeValueAsString(carUpdateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.color").value("YELLOW"))
                .andExpect(jsonPath("$.registrationNumber").value("6TAX7778"));
        verify(carService, times(1)).updateCar(carUpdateRequest);
    }

    @Test
    @Timeout(1000)
    void testCreateCar() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(carService.saveCar(carRequest)).thenReturn(carResponse);
        mockMvc.perform(post("/api/v1/cars")
                        .content(mapper.writeValueAsString(carRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.registrationNumber").value("6TAX7898"));
        verify(carService, times(1)).saveCar(carRequest);
    }

    @Test
    @Timeout(1000)
    void testSoftDeleteCar() throws Exception {
        mockMvc.perform(delete("/api/v1/cars/{id}", carRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));

        verify(carService, times(1)).softDeleteCar(carRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testFindCarById() throws Exception {
        when(carService.findCarById(carRequest.getId())).thenReturn(carResponse);
        mockMvc.perform(get("/api/v1/cars/{id}", carRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.registrationNumber").value("6TAX7898"));
        verify(carService, times(1)).findCarById(carRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testGetAllCars() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
        ArgumentCaptor<CarFilter> filterCaptor = forClass(CarFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = forClass(Pageable.class);
        when(carService.getAllCars(any(CarFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Stream.of(carResponse, secondCarResponse).toList(),
                        pageable, 2));
        mockMvc.perform(get("/api/v1/cars")
                        .param("size", "10")
                        .param("page", "0")
                        .param("sort", "id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].registrationNumber").value("7TAX4568"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
        verify(carService, times(1)).getAllCars(filterCaptor.capture(),
                pageableCaptor.capture());
    }
}
