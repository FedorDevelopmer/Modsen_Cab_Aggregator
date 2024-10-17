package com.modsen.software.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.filter.DriverFilter;
import com.modsen.software.driver.service.impl.DriverServiceImpl;
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

@WebMvcTest(DriverController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverServiceImpl driverService;

    private Driver driver;

    private Driver secondDriver;

    private DriverRequestTO driverRequest;

    private DriverResponseTO driverResponse;

    private DriverResponseTO secondDriverResponse;

    @BeforeEach
    void setUpDrivers() {
        driver = Driver.builder()
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

        secondDriver = Driver.builder()
                .id(2L)
                .name("Elina")
                .surname("Fallrell")
                .email("el.fall@mail.com")
                .phoneNumber("+111-113-113")
                .birthDate(new Date(System.currentTimeMillis()))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .rating(BigDecimal.valueOf(5))
                .gender(Gender.FEMALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .cars(new HashSet<>())
                .build();

        driverRequest = new DriverRequestTO();
        driverRequest.setId(1L);
        driverRequest.setName("John");
        driverRequest.setSurname("Conor");
        driverRequest.setGender(Gender.MALE);
        driverRequest.setEmail("john.con@mail.com");
        driverRequest.setBirthDate(new Date(System.currentTimeMillis()));
        driverRequest.setPhoneNumber("+123-123-123");
        driverRequest.setRemoveStatus(RemoveStatus.ACTIVE);
        driverRequest.setCars(new HashSet<>());

        driverResponse = new DriverResponseTO();
        driverResponse.setId(1L);
        driverResponse.setName("John");
        driverResponse.setSurname("Conor");
        driverResponse.setGender(Gender.MALE);
        driverResponse.setEmail("john.con@mail.com");
        driverResponse.setBirthDate(new Date(System.currentTimeMillis()));
        driverResponse.setPhoneNumber("+123-123-123");
        driverResponse.setRemoveStatus(RemoveStatus.ACTIVE);
        driverResponse.setRating(BigDecimal.valueOf(5));
        driverResponse.setRatingUpdateTimestamp(LocalDateTime.now());
        driverResponse.setCars(new HashSet<>());

        secondDriverResponse = new DriverResponseTO();
        secondDriverResponse.setId(2L);
        secondDriverResponse.setName("Elina");
        secondDriverResponse.setSurname("Fallrell");
        secondDriverResponse.setGender(Gender.FEMALE);
        secondDriverResponse.setEmail("el.fall@mail.com");
        secondDriverResponse.setBirthDate(new Date(System.currentTimeMillis()));
        secondDriverResponse.setPhoneNumber("+111-113-113");
        secondDriverResponse.setRemoveStatus(RemoveStatus.ACTIVE);
        secondDriverResponse.setRating(BigDecimal.valueOf(5));
        secondDriverResponse.setRatingUpdateTimestamp(LocalDateTime.now());
        secondDriverResponse.setCars(new HashSet<>());
    }

    @Test
    @Timeout(1000)
    void testUpdateDriver() throws Exception {

        DriverRequestTO driverUpdateRequest = new DriverRequestTO();
        driverUpdateRequest.setId(1L);
        driverUpdateRequest.setName("John");
        driverUpdateRequest.setSurname("Doe");
        driverUpdateRequest.setGender(Gender.MALE);
        driverUpdateRequest.setEmail("john.doe@mail.com");
        driverUpdateRequest.setBirthDate(new Date(System.currentTimeMillis()));
        driverUpdateRequest.setPhoneNumber("+122-111-122");
        driverUpdateRequest.setRemoveStatus(RemoveStatus.ACTIVE);
        driverUpdateRequest.setCars(new HashSet<>());

        DriverResponseTO driverUpdateResponse = new DriverResponseTO();
        driverUpdateResponse.setId(1L);
        driverUpdateResponse.setName("John");
        driverUpdateResponse.setSurname("Doe");
        driverUpdateResponse.setGender(Gender.MALE);
        driverUpdateResponse.setEmail("john.doe@mail.com");
        driverUpdateResponse.setBirthDate(new Date(System.currentTimeMillis()));
        driverUpdateResponse.setPhoneNumber("+122-111-122");
        driverUpdateResponse.setRemoveStatus(RemoveStatus.ACTIVE);
        driverUpdateResponse.setCars(new HashSet<>());

        ObjectMapper mapper = new ObjectMapper();
        when(driverService.updateDriver(driverUpdateRequest)).thenReturn(driverUpdateResponse);
        mockMvc.perform(put("/api/v1/drivers")
                        .content(mapper.writeValueAsString(driverUpdateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@mail.com"));
        verify(driverService, times(1)).updateDriver(driverUpdateRequest);
    }

    @Test
    @Timeout(1000)
    void testCreateDriver() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(driverService.saveDriver(driverRequest)).thenReturn(driverResponse);
        mockMvc.perform(post("/api/v1/drivers")
                        .content(mapper.writeValueAsString(driverRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.con@mail.com"));
        verify(driverService, times(1)).saveDriver(driverRequest);
    }

    @Test
    @Timeout(1000)
    void testSoftDeleteDriver() throws Exception {
        mockMvc.perform(delete("/api/v1/drivers/{id}", driverRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));

        verify(driverService, times(1)).softDeleteDriver(driverRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testFindDriverById() throws Exception {
        when(driverService.findDriverById(driverRequest.getId())).thenReturn(driverResponse);
        mockMvc.perform(get("/api/v1/drivers/{id}", driverRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.con@mail.com"));
        verify(driverService, times(1)).findDriverById(driverRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testGetAllDrivers() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
        ArgumentCaptor<DriverFilter> filterCaptor = forClass(DriverFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = forClass(Pageable.class);
        when(driverService.getAllDrivers(any(DriverFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Stream.of(driverResponse, secondDriverResponse).toList(),
                        pageable, 2));
        mockMvc.perform(get("/api/v1/drivers")
                        .param("size", "10")
                        .param("page", "0")
                        .param("sort", "id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Elina"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
        verify(driverService, times(1)).getAllDrivers(filterCaptor.capture(),
                pageableCaptor.capture());
    }
}
