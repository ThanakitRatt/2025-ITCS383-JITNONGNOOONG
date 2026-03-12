package com.itcs383.restaurant.controller;

import com.itcs383.common.dto.ApiResponse;
import com.itcs383.common.dto.MenuItemDTO;
import com.itcs383.common.dto.RestaurantDTO;
import com.itcs383.common.enums.RestaurantStatus;
import com.itcs383.restaurant.dto.CreateMenuCategoryRequest;
import com.itcs383.restaurant.dto.CreateMenuItemRequest;
import com.itcs383.restaurant.dto.CreateRestaurantRequest;
import com.itcs383.restaurant.dto.UpdateMenuItemRequest;
import com.itcs383.restaurant.entity.MenuCategory;
import com.itcs383.restaurant.service.RestaurantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Restaurant Controller
 * 
 * Comprehensive REST API for restaurant and menu management
 * Handles restaurant operations, menu management, and discovery features
 */
@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurant Management", description = "APIs for restaurant and menu management")
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);
    private static final String MSG_RESTAURANT_NOT_FOUND = "Restaurant not found";
    private static final String SORT_DISPLAY_ORDER = "displayOrder";

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // ==================== RESTAURANT ENDPOINTS ====================

    /**
     * Create new restaurant
     */
    @PostMapping
    @Operation(summary = "Create new restaurant", description = "Register a new restaurant in the system")
    public ResponseEntity<ApiResponse<RestaurantDTO>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {
        
        logger.info("Creating restaurant: {}", request.getName());
        
        try {
            RestaurantDTO restaurant = restaurantService.createRestaurant(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(restaurant, "Restaurant created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid restaurant creation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating restaurant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create restaurant: " + e.getMessage()));
        }
    }

    /**
     * Get restaurant by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant details", description = "Retrieve detailed restaurant information")
    public ResponseEntity<ApiResponse<RestaurantDTO>> getRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        logger.debug("Getting restaurant: {}", id);
        
        try {
            RestaurantDTO restaurant = restaurantService.getRestaurant(id);
            return ResponseEntity.ok(ApiResponse.success(restaurant));
        } catch (RuntimeException e) {
            logger.warn("Restaurant not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MSG_RESTAURANT_NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error fetching restaurant: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch restaurant"));
        }
    }

    /**
     * Get all restaurants with pagination and filtering
     */
    @GetMapping
    @Operation(summary = "List restaurants", description = "Get paginated list of active restaurants")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> getAllRestaurants(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Getting all restaurants - page: {}, size: {}", page, size);
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<RestaurantDTO> restaurants = restaurantService.getAllRestaurants(pageable);
            return ResponseEntity.ok(ApiResponse.success(restaurants));
        } catch (Exception e) {
            logger.error("Error fetching restaurants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch restaurants"));
        }
    }

    /**
     * Search restaurants with comprehensive filtering
     */
    @GetMapping("/search")
    @Operation(summary = "Search restaurants", description = "Search restaurants with multiple filters")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> searchRestaurants(
            @Parameter(description = "Search term") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Cuisine type") @RequestParam(required = false) String cuisineType,
            @Parameter(description = "Minimum rating") @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "Maximum delivery fee") @RequestParam(required = false) BigDecimal maxDeliveryFee,
            @Parameter(description = "Customer latitude") @RequestParam(required = false) Double latitude,
            @Parameter(description = "Customer longitude") @RequestParam(required = false) Double longitude,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Searching restaurants with filters - term: {}, cuisine: {}, location: {},{}", 
                    searchTerm, cuisineType, latitude, longitude);
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<RestaurantDTO> restaurants = restaurantService.searchRestaurants(
                searchTerm, cuisineType, minRating, maxDeliveryFee, latitude, longitude, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(restaurants));
        } catch (Exception e) {
            logger.error("Error searching restaurants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search restaurants"));
        }
    }

    /**
     * Get restaurants by cuisine type
     */
    @SuppressWarnings("null")
    @GetMapping("/cuisine/{cuisineType}")
    @Operation(summary = "Get restaurants by cuisine", description = "Filter restaurants by cuisine type")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> getRestaurantsByCuisine(
            @Parameter(description = "Cuisine type") @PathVariable String cuisineType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Getting restaurants by cuisine: {}", cuisineType);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
            Page<RestaurantDTO> restaurants = restaurantService.getRestaurantsByCuisine(cuisineType, pageable);
            return ResponseEntity.ok(ApiResponse.success(restaurants));
        } catch (Exception e) {
            logger.error("Error fetching restaurants by cuisine: {}", cuisineType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch restaurants by cuisine"));
        }
    }

    /**
     * Get top rated restaurants
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated restaurants", description = "Retrieve highest rated restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getTopRatedRestaurants(
            @Parameter(description = "Number of restaurants to return") @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("Getting top {} rated restaurants", limit);
        
        try {
            List<RestaurantDTO> restaurants = restaurantService.getTopRatedRestaurants(limit);
            return ResponseEntity.ok(ApiResponse.success(restaurants));
        } catch (Exception e) {
            logger.error("Error fetching top rated restaurants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch top rated restaurants"));
        }
    }

    /**
     * Get restaurants by owner
     */
    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get owner's restaurants", description = "Retrieve all restaurants owned by specific user")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getOwnerRestaurants(
            @Parameter(description = "Owner user ID") @PathVariable Long ownerId) {
        
        logger.debug("Getting restaurants for owner: {}", ownerId);
        
        try {
            List<RestaurantDTO> restaurants = restaurantService.getOwnerRestaurants(ownerId);
            return ResponseEntity.ok(ApiResponse.success(restaurants));
        } catch (Exception e) {
            logger.error("Error fetching owner restaurants: {}", ownerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch owner restaurants"));
        }
    }

    /**
     * Update restaurant
     */
    @SuppressWarnings("null")
    @PutMapping("/{id}")
    @Operation(summary = "Update restaurant", description = "Update restaurant information")
    public ResponseEntity<ApiResponse<RestaurantDTO>> updateRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Valid @RequestBody CreateRestaurantRequest request) {
        
        logger.info("Updating restaurant: {}", id);
        
        try {
            RestaurantDTO restaurant = restaurantService.updateRestaurant(id, request);
            return ResponseEntity.ok(ApiResponse.success(restaurant, "Restaurant updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid restaurant update request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid request: " + e.getMessage()));
        } catch (RuntimeException e) {
            logger.warn("Restaurant not found for update: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MSG_RESTAURANT_NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error updating restaurant: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update restaurant"));
        }
    }

    /**
     * Update restaurant status (Admin only)
     */
    @SuppressWarnings("null")
    @PutMapping("/{id}/status")
    @Operation(summary = "Update restaurant status", description = "Admin operation to approve/reject restaurants")
    public ResponseEntity<ApiResponse<RestaurantDTO>> updateRestaurantStatus(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam RestaurantStatus status,
            @Parameter(description = "Status change reason") @RequestParam(required = false) String reason) {
        
        logger.info("Updating restaurant {} status to: {}", id, status);
        
        try {
            RestaurantDTO restaurant = restaurantService.updateRestaurantStatus(id, status, reason);
            return ResponseEntity.ok(ApiResponse.success(restaurant, "Restaurant status updated successfully"));
        } catch (RuntimeException e) {
            logger.warn("Restaurant not found for status update: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MSG_RESTAURANT_NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error updating restaurant status: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update restaurant status"));
        }
    }

    /**
     * Toggle restaurant availability
     */
    @SuppressWarnings("null")
    @PutMapping("/{id}/availability")
    @Operation(summary = "Toggle restaurant availability", description = "Enable/disable order acceptance")
    public ResponseEntity<ApiResponse<RestaurantDTO>> toggleRestaurantAvailability(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Parameter(description = "Accept orders") @RequestParam Boolean acceptsOrders) {
        
        logger.info("Toggling restaurant {} availability to: {}", id, acceptsOrders);
        
        try {
            RestaurantDTO restaurant = restaurantService.toggleRestaurantAvailability(id, acceptsOrders);
            return ResponseEntity.ok(ApiResponse.success(restaurant, "Restaurant availability updated"));
        } catch (RuntimeException e) {
            logger.warn("Restaurant not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MSG_RESTAURANT_NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error toggling restaurant availability: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update restaurant availability"));
        }
    }

    // ==================== MENU ENDPOINTS ====================

    /**
     * Get restaurant menu
     */
    @SuppressWarnings("null")
    @GetMapping("/{id}/menu")
    @Operation(summary = "Get restaurant menu", description = "Retrieve paginated menu items for a restaurant")
    public ResponseEntity<ApiResponse<Page<MenuItemDTO>>> getRestaurantMenu(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size) {
        
        logger.debug("Getting menu for restaurant: {}", id);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(SORT_DISPLAY_ORDER, "name"));
            Page<MenuItemDTO> menuItems = restaurantService.getRestaurantMenu(id, pageable);
            return ResponseEntity.ok(ApiResponse.success(menuItems));
        } catch (Exception e) {
            logger.error("Error fetching restaurant menu: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch restaurant menu"));
        }
    }

    /**
     * Add menu item
     */
    @SuppressWarnings("null")
    @PostMapping("/{id}/menu")
    @Operation(summary = "Add menu item", description = "Add new item to restaurant menu")
    public ResponseEntity<ApiResponse<MenuItemDTO>> addMenuItem(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Valid @RequestBody CreateMenuItemRequest request) {
        
        logger.info("Adding menu item '{}' to restaurant {}", request.getName(), id);
        
        try {
            MenuItemDTO menuItem = restaurantService.addMenuItem(id, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(menuItem, "Menu item added successfully"));
        } catch (RuntimeException e) {
            logger.warn("Error adding menu item: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding menu item to restaurant: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add menu item"));
        }
    }

    /**
     * Update menu item
     */
    @SuppressWarnings("null")
    @PutMapping("/{id}/menu/{itemId}")
    @Operation(summary = "Update menu item", description = "Update an existing menu item")
    public ResponseEntity<ApiResponse<MenuItemDTO>> updateMenuItem(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Parameter(description = "Menu item ID") @PathVariable Long itemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        
        logger.info("Updating menu item {} in restaurant {}", itemId, id);
        
        try {
            MenuItemDTO menuItem = restaurantService.updateMenuItem(id, itemId, request);
            return ResponseEntity.ok(ApiResponse.success(menuItem, "Menu item updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid menu item update request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            logger.warn("Menu item not found: {}", itemId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Menu item not found"));
        } catch (Exception e) {
            logger.error("Error updating menu item: {}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update menu item"));
        }
    }

    /**
     * Delete menu item
     */
    @DeleteMapping("/{id}/menu/{itemId}")
    @Operation(summary = "Delete menu item", description = "Remove a menu item from the restaurant")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Parameter(description = "Menu item ID") @PathVariable Long itemId) {
        
        logger.info("Deleting menu item {} from restaurant {}", itemId, id);
        
        try {
            restaurantService.deleteMenuItem(id, itemId);
            return ResponseEntity.ok(ApiResponse.success(null, "Menu item deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Menu item not found"));
        } catch (Exception e) {
            logger.error("Error deleting menu item: {}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete menu item"));
        }
    }

    /**
     * Delete restaurant
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete restaurant", description = "Permanently delete a restaurant")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        logger.info("Deleting restaurant: {}", id);
        
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Restaurant deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MSG_RESTAURANT_NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error deleting restaurant: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete restaurant"));
        }
    }

    /**
     * Get featured menu items
     */
    @GetMapping("/{id}/menu/featured")
    @Operation(summary = "Get featured menu items", description = "Retrieve featured items from restaurant menu")
    public ResponseEntity<ApiResponse<List<MenuItemDTO>>> getFeaturedMenuItems(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        logger.debug("Getting featured items for restaurant: {}", id);
        
        try {
            List<MenuItemDTO> featuredItems = restaurantService.getFeaturedMenuItems(id);
            return ResponseEntity.ok(ApiResponse.success(featuredItems));
        } catch (Exception e) {
            logger.error("Error fetching featured items: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch featured items"));
        }
    }

    /**
     * Search menu items
     */
    @SuppressWarnings("null")
    @GetMapping("/{id}/menu/search")
    @Operation(summary = "Search menu items", description = "Search items within restaurant menu")
    public ResponseEntity<ApiResponse<Page<MenuItemDTO>>> searchMenuItems(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Searching menu items for restaurant {} with term: {}", id, searchTerm);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
            Page<MenuItemDTO> menuItems = restaurantService.searchMenuItems(id, searchTerm, pageable);
            return ResponseEntity.ok(ApiResponse.success(menuItems));
        } catch (Exception e) {
            logger.error("Error searching menu items: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search menu items"));
        }
    }

    // ==================== CATEGORY ENDPOINTS ====================

    /**
     * Get restaurant categories
     */
    @GetMapping("/{id}/categories")
    @Operation(summary = "Get menu categories", description = "Retrieve menu categories for restaurant")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRestaurantCategories(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        logger.debug("Getting categories for restaurant: {}", id);
        
        try {
            List<MenuCategory> categories = restaurantService.getRestaurantCategories(id);
            
            // Convert to simple DTOs to avoid lazy loading issues
            List<Map<String, Object>> categoryDtos = categories.stream()
                .map(cat -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", cat.getId());
                    dto.put("name", cat.getName());
                    dto.put("description", cat.getDescription());
                    dto.put("isActive", cat.getIsActive());
                    dto.put(SORT_DISPLAY_ORDER, cat.getDisplayOrder());
                    dto.put("imageUrl", cat.getImageUrl());
                    dto.put("createdAt", cat.getCreatedAt());
                    dto.put("updatedAt", cat.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(categoryDtos));
        } catch (Exception e) {
            logger.error("Error fetching restaurant categories: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch categories"));
        }
    }

    /**
     * Add menu category
     */
    @SuppressWarnings("null")
    @PostMapping("/{id}/categories")
    @Operation(summary = "Add menu category", description = "Add new category to restaurant menu")
    public ResponseEntity<ApiResponse<MenuCategory>> addMenuCategory(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Valid @RequestBody CreateMenuCategoryRequest request) {
        
        logger.info("Adding category '{}' to restaurant {}", request.getName(), id);
        
        try {
            MenuCategory category = restaurantService.addMenuCategory(id, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "Category added successfully"));
        } catch (RuntimeException e) {
            logger.warn("Error adding category: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding category to restaurant: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add category"));
        }
    }

    /**
     * Get menu items by category
     */
    @SuppressWarnings("null")
    @GetMapping("/categories/{categoryId}/items")
    @Operation(summary = "Get items by category", description = "Retrieve menu items for specific category")
    public ResponseEntity<ApiResponse<Page<MenuItemDTO>>> getMenuItemsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("Getting menu items for category: {}", categoryId);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(SORT_DISPLAY_ORDER, "name"));
            Page<MenuItemDTO> menuItems = restaurantService.getMenuItemsByCategory(categoryId, pageable);
            return ResponseEntity.ok(ApiResponse.success(menuItems));
        } catch (Exception e) {
            logger.error("Error fetching menu items by category: {}", categoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch menu items"));
        }
    }

    /**
     * Delete menu category
     */
    @DeleteMapping("/{id}/categories/{categoryId}")
    @Operation(summary = "Delete menu category", description = "Remove a category from the restaurant menu")
    public ResponseEntity<ApiResponse<Void>> deleteMenuCategory(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        
        logger.info("Deleting category {} from restaurant {}", categoryId, id);
        
        try {
            restaurantService.deleteMenuCategory(id, categoryId);
            return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Category not found"));
        } catch (Exception e) {
            logger.error("Error deleting category: {}", categoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete category"));
        }
    }

    // ==================== UTILITY ENDPOINTS ====================

    /**
     * Get available cuisine types
     */
    @GetMapping("/cuisine-types")
    @Operation(summary = "Get cuisine types", description = "List all available cuisine types")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableCuisineTypes() {
        
        logger.debug("Getting available cuisine types");
        
        try {
            List<String> cuisineTypes = restaurantService.getAvailableCuisineTypes();
            return ResponseEntity.ok(ApiResponse.success(cuisineTypes));
        } catch (Exception e) {
            logger.error("Error fetching cuisine types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch cuisine types"));
        }
    }

    /**
     * Get restaurants pending approval (Admin only)
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending restaurants", description = "Admin endpoint to view restaurants awaiting approval")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getPendingRestaurants() {
        
        logger.debug("Getting restaurants pending approval");
        
        try {
            List<RestaurantDTO> pendingRestaurants = restaurantService.getPendingRestaurants();
            return ResponseEntity.ok(ApiResponse.success(pendingRestaurants));
        } catch (Exception e) {
            logger.error("Error fetching pending restaurants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch pending restaurants"));
        }
    }

    /**
     * Admin statistics (Admin only)
     */
    @GetMapping("/stats")
    @Operation(summary = "Get restaurant stats", description = "Admin endpoint to view restaurant counts")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getRestaurantStats() {
        try {
            return ResponseEntity.ok(ApiResponse.success(restaurantService.getRestaurantStats()));
        } catch (Exception e) {
            logger.error("Error fetching restaurant stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch restaurant stats"));
        }
    }
}
