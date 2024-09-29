package com.modsen.software.passenger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.filter.PassengerFilter;
import com.modsen.software.passenger.service.impl.PassengerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@WebMvcTest(PassengerController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class PassengerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PassengerServiceImpl passengerService;

    private Passenger passenger;

    private Passenger secondPassenger;

    private PassengerRequestTO passengerRequest;

    private PassengerResponseTO passengerResponse;

    private PassengerResponseTO secondPassengerResponse;

    @BeforeEach
    void setUpPassengers() {
        passenger = Passenger.builder()
                .id(1L)
                .name("Andrew")
                .phoneNumber("+123-123-123")
                .email("andrew.tdk@mail.com")
                .gender(Gender.MALE)
                .rating(BigDecimal.valueOf(5))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        secondPassenger = Passenger.builder()
                .id(2L)
                .name("Marry")
                .phoneNumber("+123-111-111")
                .email("marry.el@mail.com")
                .gender(Gender.FEMALE)
                .rating(BigDecimal.valueOf(5))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .removeStatus(RemoveStatus.ACTIVE)
                .build();

        passengerRequest = new PassengerRequestTO();
        passengerRequest.setId(1L);
        passengerRequest.setName("Andrew");
        passengerRequest.setPhoneNumber("+123-123-123");
        passengerRequest.setEmail("andrew.tdk@mail.com");
        passengerRequest.setGender(Gender.MALE);
        passengerRequest.setRemoveStatus(RemoveStatus.ACTIVE);

        passengerResponse = new PassengerResponseTO();
        passengerResponse.setId(1L);
        passengerResponse.setName("Andrew");
        passengerResponse.setPhoneNumber("+123-123-123");
        passengerResponse.setEmail("andrew.tdk@mail.com");
        passengerResponse.setGender(Gender.MALE);
        passengerResponse.setRating(BigDecimal.valueOf(5));
        passengerResponse.setRatingUpdateTimestamp(LocalDateTime.now());
        passengerResponse.setRemoveStatus(RemoveStatus.ACTIVE);

        secondPassengerResponse = new PassengerResponseTO();
        secondPassengerResponse.setId(2L);
        secondPassengerResponse.setName("Marry");
        secondPassengerResponse.setPhoneNumber("+123-111-111");
        secondPassengerResponse.setEmail("marry.el@mail.com");
        secondPassengerResponse.setGender(Gender.FEMALE);
        secondPassengerResponse.setRating(BigDecimal.valueOf(5));
        secondPassengerResponse.setRatingUpdateTimestamp(LocalDateTime.now());
        secondPassengerResponse.setRemoveStatus(RemoveStatus.ACTIVE);

    }

    @Test
    @Timeout(1000)
    void testUpdatePassenger() throws Exception{

        PassengerRequestTO passengerUpdateRequest = new PassengerRequestTO();
        passengerUpdateRequest.setId(1L);
        passengerUpdateRequest.setName("Andrew");
        passengerUpdateRequest.setPhoneNumber("+124-144-523");
        passengerUpdateRequest.setEmail("andrew.new@mail.com");
        passengerUpdateRequest.setGender(Gender.MALE);
        passengerUpdateRequest.setRemoveStatus(RemoveStatus.ACTIVE);

        PassengerResponseTO passengerUpdateResponse = new PassengerResponseTO();
        passengerUpdateResponse.setId(1L);
        passengerUpdateResponse.setName("Andrew");
        passengerUpdateResponse.setPhoneNumber("+124-144-523");
        passengerUpdateResponse.setEmail("andrew.new@mail.com");
        passengerUpdateResponse.setRating(BigDecimal.valueOf(5));
        passengerUpdateResponse.setRatingUpdateTimestamp(LocalDateTime.now());
        passengerUpdateResponse.setGender(Gender.MALE);
        passengerUpdateResponse.setRemoveStatus(RemoveStatus.ACTIVE);

        ObjectMapper mapper = new ObjectMapper();
        when(passengerService.updatePassenger(passengerUpdateRequest)).thenReturn(passengerUpdateResponse);
        mockMvc.perform(put("/api/v1/passengers")
                        .content(mapper.writeValueAsString(passengerUpdateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name").value("Andrew"))
                .andExpect(jsonPath("$.email").value("andrew.new@mail.com"));
        verify(passengerService,times(1)).updatePassenger(passengerUpdateRequest);
    }

    @Test
    @Timeout(1000)
    void testCreatePassenger() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        when(passengerService.savePassenger(passengerRequest)).thenReturn(passengerResponse);
        mockMvc.perform(post("/api/v1/passengers")
                        .content(mapper.writeValueAsString(passengerRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Andrew"))
                .andExpect(jsonPath("$.email").value("andrew.tdk@mail.com"));
        verify(passengerService,times(1)).savePassenger(passengerRequest);
    }

    @Test
    @Timeout(1000)
    void testSoftDeletePassenger() throws Exception{
        mockMvc.perform(delete("/api/v1/passengers/{id}",passengerRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));

        verify(passengerService,times(1)).softDeletePassenger(passengerRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testFindPassengerById() throws Exception{
        when(passengerService.findPassengerById(passengerRequest.getId())).thenReturn(passengerResponse);
        mockMvc.perform(get("/api/v1/passengers/{id}",passengerRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Andrew"))
                .andExpect(jsonPath("$.email").value("andrew.tdk@mail.com"));
        verify(passengerService,times(1)).findPassengerById(passengerRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testGetAllPassengers() throws Exception{
        Pageable pageable = PageRequest.of(0,10, Sort.Direction.ASC,"id");
        ArgumentCaptor<PassengerFilter> filterCaptor = forClass(PassengerFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = forClass(Pageable.class);
        when(passengerService.getAllPassengers(any(PassengerFilter.class),any(Pageable.class)))
                .thenReturn(new PageImpl<>(Stream.of(passengerResponse, secondPassengerResponse).toList(),
                        pageable,2));
        mockMvc.perform(get("/api/v1/passengers")
                        .param("size","10")
                        .param("page","0")
                        .param("sort","id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Marry"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
        verify(passengerService,times(1)).getAllPassengers(filterCaptor.capture(),
                pageableCaptor.capture());
    }
}
