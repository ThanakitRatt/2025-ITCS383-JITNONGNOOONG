package com.itcs383.restaurant.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcs383.common.dto.RestaurantDTO;
import com.itcs383.common.enums.RestaurantStatus;
import com.itcs383.restaurant.dto.CreateRestaurantRequest;
import com.itcs383.restaurant.service.RestaurantService;

@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    private RestaurantDTO mockRestaurant;

    @BeforeEach
    void setUp() {
        mockRestaurant = RestaurantDTO.builder()
                .id(1L)
                .name("Test Restaurant")
                .description("Test Description")
                .cuisineType("THAI")
                .phoneNumber("+66-2-123-4567")
                .email("test@restaurant.com")
                .address("123 Test St")
                .latitude(13.7563)
                .longitude(100.5018)
                .deliveryRadiusKm(5.0)
                .deliveryFee(new BigDecimal("25.00"))
                .minimumOrderAmount(new BigDecimal("100.00"))
                .status(RestaurantStatus.APPROVED)
                .acceptsOrders(true)
                .averageRating(new BigDecimal("4.5"))
                .totalReviews(100)
                .ownerId(1L)
                .build();
    }

    @Test
    void createRestaurant_ShouldReturnCreatedRestaurant() throws Exception {
        CreateRestaurantRequest request = CreateRestaurantRequest.builder()
                .name("Test Restaurant")
                .description("Test Description")
                .cuisineType("THAI")
                .phoneNumber("+66-2-123-4567")
                .email("test@restaurant.com")
                .address("123 Test St")
                .latitude(13.7563)
                .longitude(100.5018)
                .ownerId(1L)
                .build();

        when(restaurantService.createRestaurant(any(CreateRestaurantRequest.class)))
                .thenReturn(mockRestaurant);

        mockMvc.perform(post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.data.cuisineType").value("THAI"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void getRestaurant_ShouldReturnRestaurant() throws Exception {
        when(restaurantService.getRestaurant(1L)).thenReturn(mockRestaurant);

        mockMvc.perform(get("/api/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Restaurant"));
    }

    @Test
    void getAllRestaurants_ShouldReturnPagedRestaurants() throws Exception {
        Page<RestaurantDTO> page = new PageImpl<>(
                Arrays.asList(mockRestaurant),
                PageRequest.of(0, 20),
                1
        );

        when(restaurantService.getAllRestaurants(any())).thenReturn(page);

        mockMvc.perform(get("/api/restaurants")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Restaurant"));
    }

    @Test
    void updateRestaurant_ShouldReturnUpdatedRestaurant() throws Exception {
        CreateRestaurantRequest request = CreateRestaurantRequest.builder()
                .name("Updated Restaurant")
                .description("Updated Description")
                .cuisineType("ITALIAN")
                .phoneNumber("+66-2-234-5678")
                .email("updated@restaurant.com")
                .address("456 Updated St")
                .build();

        RestaurantDTO updatedRestaurant = RestaurantDTO.builder()
                .id(1L)
                .name("Updated Restaurant")
                .description("Updated Description")
                .cuisineType("ITALIAN")
                .phoneNumber("+66-2-234-5678")
                .email("updated@restaurant.com")
                .address("456 Updated St")
                .status(RestaurantStatus.APPROVED)
                .build();

        when(restaurantService.updateRestaurant(anyLong(), any(CreateRestaurantRequest.class)))
                .thenReturn(updatedRestaurant);

        mockMvc.perform(put("/api/restaurants/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Restaurant"))
                .andExpect(jsonPath("$.data.cuisineType").value("ITALIAN"));
    }

    // TODO: Re-enable these tests after fixing MenuItemDTO issues
    /*
    @Test
    void addMenuItem_ShouldReturnCreatedMenuItem() throws Exception {
        CreateMenuItemRequest request = CreateMenuItemRequest.builder()
                .name("Test Item")
                .description("Test item description")
                .price(new BigDecimal("150.00"))
                .categoryId(1L)
                .preparationTime(15)
                .build();

        when(restaurantService.addMenuItem(anyLong(), any(CreateMenuItemRequest.class)))
                .thenReturn(mockMenuItem);

        mockMvc.perform(post("/api/restaurants/1/menu-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpected(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Item"))
                .andExpect(jsonPath("$.data.price").value(150.00));
    }

    @Test
    void getRestaurantMenu_ShouldReturnMenuItems() throws Exception {
        Page<MenuItemDTO> page = new PageImpl<>(
                Arrays.asList(mockMenuItem),
                PageRequest.of(0, 20),
                1
        );

        when(restaurantService.getRestaurantMenu(anyLong(), any())).thenReturn(page);

        mockMvc.perform(get("/api/restaurants/1/menu")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Item"));
    }
    */

    @Test
    void updateRestaurantStatus_ShouldReturnUpdatedRestaurant() throws Exception {
        RestaurantDTO updatedRestaurant = RestaurantDTO.builder()
                .id(1L)
                .name("Test Restaurant")
                .status(RestaurantStatus.SUSPENDED)
                .build();

        when(restaurantService.updateRestaurantStatus(anyLong(), any(RestaurantStatus.class), any()))
                .thenReturn(updatedRestaurant);

        mockMvc.perform(put("/api/restaurants/1/status")
                .param("status", "SUSPENDED")
                .param("reason", "Policy violation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test
    void toggleRestaurantAvailability_ShouldReturnUpdatedRestaurant() throws Exception {
        RestaurantDTO updatedRestaurant = RestaurantDTO.builder()
                .id(1L)
                .name("Test Restaurant")
                .acceptsOrders(false)
                .build();

        when(restaurantService.toggleRestaurantAvailability(anyLong(), any(Boolean.class)))
                .thenReturn(updatedRestaurant);

        mockMvc.perform(put("/api/restaurants/1/availability")
                .param("acceptsOrders", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acceptsOrders").value(false));
    }

    @Test
    void searchRestaurants_ShouldReturnFilteredRestaurants() throws Exception {
        Page<RestaurantDTO> page = new PageImpl<>(
                Arrays.asList(mockRestaurant),
                PageRequest.of(0, 20),
                1
        );

        when(restaurantService.searchRestaurants(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/restaurants/search")
                .param("searchTerm", "Thai")
                .param("cuisineType", "THAI")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].cuisineType").value("THAI"));
    }

    @Test
    void getTopRatedRestaurants_ShouldReturnTopRestaurants() throws Exception {
        List<RestaurantDTO> topRestaurants = Arrays.asList(mockRestaurant);

        when(restaurantService.getTopRatedRestaurants(any(Integer.class)))
                .thenReturn(topRestaurants);

        mockMvc.perform(get("/api/restaurants/top-rated")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].averageRating").value(4.5));
    }

    @Test
    void getAvailableCuisineTypes_ShouldReturnCuisineList() throws Exception {
        List<String> cuisineTypes = Arrays.asList("THAI", "ITALIAN", "AMERICAN", "CHINESE");

        when(restaurantService.getAvailableCuisineTypes()).thenReturn(cuisineTypes);

        mockMvc.perform(get("/api/restaurants/cuisine-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").value(cuisineTypes));
    }
}
