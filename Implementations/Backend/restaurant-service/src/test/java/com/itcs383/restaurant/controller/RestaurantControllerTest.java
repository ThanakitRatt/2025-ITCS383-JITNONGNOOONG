package com.itcs383.restaurant.controller;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcs383.common.dto.RestaurantDTO;
import com.itcs383.common.enums.RestaurantStatus;
import com.itcs383.restaurant.config.JpaConfig;
import com.itcs383.restaurant.dto.CreateRestaurantRequest;
import com.itcs383.restaurant.service.RestaurantService;

@WebMvcTest(controllers = RestaurantController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class))
@SuppressWarnings("null")
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
                .deliveryFee(new BigDecimal("25.00"))
                .minimumOrderAmount(new BigDecimal("100.00"))
                .openingTime(LocalTime.of(9, 0))
                .closingTime(LocalTime.of(22, 0))
                .estimatedDeliveryTime(30)
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
                .latitude(13.7563)
                .longitude(100.5018)
                .deliveryFee(new BigDecimal("30.00"))
                .minimumOrderAmount(new BigDecimal("150.00"))
                .openingTime(LocalTime.of(10, 0))
                .closingTime(LocalTime.of(23, 0))
                .estimatedDeliveryTime(45)
                .ownerId(1L)
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
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0]").value("THAI"))
                .andExpect(jsonPath("$.data[1]").value("ITALIAN"))
                .andExpect(jsonPath("$.data[2]").value("AMERICAN"))
                .andExpect(jsonPath("$.data[3]").value("CHINESE"));
    }

    @Test
    void addMenuItem_ShouldReturnCreatedMenuItem() throws Exception {
        com.itcs383.common.dto.MenuItemDTO mockItem = new com.itcs383.common.dto.MenuItemDTO(
                1L, "Pad Thai", "Stir-fried noodles", new BigDecimal("120.00"));

        when(restaurantService.addMenuItem(anyLong(),
                any(com.itcs383.restaurant.dto.CreateMenuItemRequest.class)))
                .thenReturn(mockItem);

        String body = "{\"name\":\"Pad Thai\",\"description\":\"Stir-fried noodles\",\"price\":120.00}";

        mockMvc.perform(post("/api/restaurants/1/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Pad Thai"));
    }

    @Test
    void updateMenuItem_ShouldReturnUpdatedMenuItem() throws Exception {
        com.itcs383.common.dto.MenuItemDTO mockItem = new com.itcs383.common.dto.MenuItemDTO(
                1L, "Updated Pad Thai", "New recipe", new BigDecimal("135.00"));

        when(restaurantService.updateMenuItem(anyLong(), anyLong(),
                any(com.itcs383.restaurant.dto.UpdateMenuItemRequest.class)))
                .thenReturn(mockItem);

        mockMvc.perform(put("/api/restaurants/1/menu/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Pad Thai"));
    }

    @Test
    void addMenuCategory_ShouldReturnCreatedCategory() throws Exception {
        com.itcs383.restaurant.entity.MenuCategory mockCat =
                new com.itcs383.restaurant.entity.MenuCategory("Noodles", "Noodle dishes", null);

        when(restaurantService.addMenuCategory(anyLong(),
                any(com.itcs383.restaurant.dto.CreateMenuCategoryRequest.class)))
                .thenReturn(mockCat);

        String body = "{\"name\":\"Noodles\",\"description\":\"Noodle dishes\"}";

        mockMvc.perform(post("/api/restaurants/1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Noodles"));
    }

    @Test
    void getRestaurantsByCuisine_ShouldReturnFilteredRestaurants() throws Exception {
        Page<RestaurantDTO> page = new PageImpl<>(
                Arrays.asList(mockRestaurant),
                PageRequest.of(0, 20),
                1
        );

        when(restaurantService.getRestaurantsByCuisine(eq("THAI"), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/restaurants/cuisine/THAI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].cuisineType").value("THAI"));
    }

    @Test
    void getOwnerRestaurants_ShouldReturnOwnerRestaurants() throws Exception {
        List<RestaurantDTO> restaurants = Arrays.asList(mockRestaurant);

        when(restaurantService.getOwnerRestaurants(1L)).thenReturn(restaurants);

        mockMvc.perform(get("/api/restaurants/owner/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Test Restaurant"));
    }

    @Test
    void getRestaurantMenu_ShouldReturnPagedMenu() throws Exception {
        com.itcs383.common.dto.MenuItemDTO menuItem =
                new com.itcs383.common.dto.MenuItemDTO(1L, "Pad Thai", "Stir-fried noodles",
                        new BigDecimal("120.00"));
        Page<com.itcs383.common.dto.MenuItemDTO> menuPage = new PageImpl<>(
                Arrays.asList(menuItem), PageRequest.of(0, 50), 1);

        when(restaurantService.getRestaurantMenu(anyLong(), any())).thenReturn(menuPage);

        mockMvc.perform(get("/api/restaurants/1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Pad Thai"));
    }

    @Test
    void deleteMenuItem_ShouldReturnOk() throws Exception {
        doNothing().when(restaurantService).deleteMenuItem(anyLong(), anyLong());

        mockMvc.perform(delete("/api/restaurants/1/menu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteRestaurant_ShouldReturnOk() throws Exception {
        doNothing().when(restaurantService).deleteRestaurant(anyLong());

        mockMvc.perform(delete("/api/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
