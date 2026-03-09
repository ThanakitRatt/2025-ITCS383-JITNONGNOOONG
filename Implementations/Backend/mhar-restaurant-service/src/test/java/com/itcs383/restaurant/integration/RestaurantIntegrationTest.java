package com.itcs383.restaurant.integration;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcs383.common.enums.RestaurantStatus;
import com.itcs383.restaurant.dto.CreateRestaurantRequest;
import com.itcs383.restaurant.entity.Restaurant;
import com.itcs383.restaurant.repository.RestaurantRepository;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class RestaurantIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        restaurantRepository.deleteAll();
    }

    @Test
    void createRestaurant_IntegrationTest() throws Exception {
        CreateRestaurantRequest request = CreateRestaurantRequest.builder()
                .name("Integration Test Restaurant")
                .description("Test restaurant for integration testing")
                .cuisineType("THAI")
                .phoneNumber("+66-2-123-4567")
                .email("integration@test.com")
                .address("123 Integration Test St")
                .latitude(13.7563)
                .longitude(100.5018)
                .deliveryFee(new BigDecimal("25.00"))
                .minimumOrderAmount(new BigDecimal("100.00"))
                .ownerId(1L)
                .build();

        mockMvc.perform(post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Integration Test Restaurant"))
                .andExpect(jsonPath("$.data.cuisineType").value("THAI"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // Verify restaurant was saved to database
        assert restaurantRepository.count() == 1;
        Restaurant savedRestaurant = restaurantRepository.findAll().get(0);
        assert savedRestaurant.getName().equals("Integration Test Restaurant");
        assert savedRestaurant.getCuisineType().equals("THAI");
        assert savedRestaurant.getStatus() == RestaurantStatus.PENDING;
    }

    @Test
    void fullRestaurantLifecycle_IntegrationTest() throws Exception {
        // Create restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Lifecycle Test Restaurant");
        restaurant.setDescription("Test restaurant for lifecycle testing");
        restaurant.setCuisineType("ITALIAN");
        restaurant.setPhoneNumber("+66-2-234-5678");
        restaurant.setEmail("lifecycle@test.com");
        restaurant.setAddress("456 Lifecycle Test St");
        restaurant.setLatitude(13.7307);
        restaurant.setLongitude(100.5418);
        restaurant.setDeliveryFee(new BigDecimal("30.00"));
        restaurant.setMinimumOrderAmount(new BigDecimal("150.00"));
        restaurant.setStatus(RestaurantStatus.PENDING);
        restaurant.setAcceptsOrders(false);
        restaurant.setOwnerId(1L);
        
        restaurant = restaurantRepository.save(restaurant);

        // Get restaurant
        mockMvc.perform(get("/api/restaurants/" + restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Lifecycle Test Restaurant"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // Update restaurant status to APPROVED
        mockMvc.perform(put("/api/restaurants/" + restaurant.getId() + "/status")
                .param("status", "APPROVED")
                .param("reason", "Restaurant meets all requirements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // Toggle restaurant availability
        mockMvc.perform(put("/api/restaurants/" + restaurant.getId() + "/availability")
                .param("acceptsOrders", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acceptsOrders").value(true));

        // Update restaurant details
        CreateRestaurantRequest updateRequest = CreateRestaurantRequest.builder()
                .name("Updated Lifecycle Restaurant")
                .description("Updated description")
                .cuisineType("ITALIAN")
                .phoneNumber("+66-2-234-5678")
                .email("updated@test.com")
                .address("456 Updated Test St")
                .build();

        mockMvc.perform(put("/api/restaurants/" + restaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Lifecycle Restaurant"))
                .andExpect(jsonPath("$.data.email").value("updated@test.com"));

        // Verify final state in database
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).orElse(null);
        assert updatedRestaurant != null;
        assert updatedRestaurant.getName().equals("Updated Lifecycle Restaurant");
        assert updatedRestaurant.getEmail().equals("updated@test.com");
        assert updatedRestaurant.getStatus() == RestaurantStatus.APPROVED;
        assert updatedRestaurant.getAcceptsOrders();
    }

    @Test
    void restaurantSearch_IntegrationTest() throws Exception {
        // Create test restaurants
        Restaurant thaiRestaurant = createTestRestaurant("Thai Delicious", "THAI", RestaurantStatus.APPROVED);
        Restaurant italianRestaurant = createTestRestaurant("Italian Kitchen", "ITALIAN", RestaurantStatus.APPROVED);
        Restaurant pendingRestaurant = createTestRestaurant("Pending Restaurant", "AMERICAN", RestaurantStatus.PENDING);

        restaurantRepository.saveAll(java.util.Arrays.asList(thaiRestaurant, italianRestaurant, pendingRestaurant));

        // Test getting all approved restaurants
        mockMvc.perform(get("/api/restaurants")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2)); // Only approved restaurants

        // Test search by cuisine type
        mockMvc.perform(get("/api/restaurants/search")
                .param("cuisineType", "THAI")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].cuisineType").value("THAI"));

        // Test search by term
        mockMvc.perform(get("/api/restaurants/search")
                .param("searchTerm", "Italian")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Italian Kitchen"));
    }

    @Test
    void getAvailableCuisineTypes_IntegrationTest() throws Exception {
        // Create restaurants with different cuisine types
        Restaurant thaiRestaurant = createTestRestaurant("Thai Restaurant", "THAI", RestaurantStatus.APPROVED);
        Restaurant italianRestaurant = createTestRestaurant("Italian Restaurant", "ITALIAN", RestaurantStatus.APPROVED);
        Restaurant americanRestaurant = createTestRestaurant("American Restaurant", "AMERICAN", RestaurantStatus.APPROVED);

        restaurantRepository.saveAll(java.util.Arrays.asList(thaiRestaurant, italianRestaurant, americanRestaurant));

        mockMvc.perform(get("/api/restaurants/cuisine-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    private Restaurant createTestRestaurant(String name, String cuisineType, RestaurantStatus status) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription("Test restaurant: " + name);
        restaurant.setCuisineType(cuisineType);
        restaurant.setPhoneNumber("+66-2-123-4567");
        restaurant.setEmail("test@" + name.toLowerCase().replace(" ", "") + ".com");
        restaurant.setAddress("123 " + name + " St");
        restaurant.setLatitude(13.7563);
        restaurant.setLongitude(100.5018);
        restaurant.setDeliveryFee(new BigDecimal("25.00"));
        restaurant.setMinimumOrderAmount(new BigDecimal("100.00"));
        restaurant.setStatus(status);
        restaurant.setAcceptsOrders(true);
        restaurant.setAverageRating(new BigDecimal("4.0"));
        restaurant.setTotalReviews(10);
        restaurant.setOwnerId(1L);
        return restaurant;
    }
}
