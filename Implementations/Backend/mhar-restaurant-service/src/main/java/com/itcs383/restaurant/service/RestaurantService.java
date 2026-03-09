package com.itcs383.restaurant.service;

import com.itcs383.common.dto.MenuItemDTO;
import com.itcs383.common.dto.RestaurantDTO;
import com.itcs383.common.enums.RestaurantStatus;
import com.itcs383.restaurant.dto.CreateMenuCategoryRequest;
import com.itcs383.restaurant.dto.CreateMenuItemRequest;
import com.itcs383.restaurant.dto.CreateRestaurantRequest;
import com.itcs383.restaurant.entity.MenuCategory;
import com.itcs383.restaurant.entity.MenuItem;
import com.itcs383.restaurant.entity.Restaurant;
import com.itcs383.restaurant.repository.MenuCategoryRepository;
import com.itcs383.restaurant.repository.MenuItemRepository;
import com.itcs383.restaurant.repository.RestaurantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Restaurant Service
 * 
 * Comprehensive business logic for restaurant management, menu operations,
 * and restaurant discovery with caching and performance optimization
 */
@Service
@Transactional
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    // ==================== RESTAURANT MANAGEMENT ====================

    /**
     * Create new restaurant
     */
    public RestaurantDTO createRestaurant(CreateRestaurantRequest request) {
        logger.info("Creating new restaurant: {}", request.getName());

        // Validation
        validateRestaurantRequest(request);
        
        // Check for duplicate restaurant name for same owner
        if (restaurantRepository.existsByNameAndOwnerId(request.getName(), request.getOwnerId())) {
            throw new IllegalArgumentException("Restaurant with this name already exists for this owner");
        }

        // Create restaurant entity
        Restaurant restaurant = new Restaurant(
            request.getName(),
            request.getDescription(),
            request.getCuisineType(),
            request.getAddress(),
            request.getLatitude(),
            request.getLongitude(),
            request.getPhoneNumber(),
            request.getOpeningTime(),
            request.getClosingTime(),
            request.getOwnerId(),
            request.getMinimumOrderAmount(),
            request.getDeliveryFee(),
            request.getEstimatedDeliveryTime()
        );

        restaurant.setEmail(request.getEmail());
        restaurant.setLogoUrl(request.getLogoUrl());
        restaurant.setCoverImageUrl(request.getCoverImageUrl());
        restaurant.setStatus(RestaurantStatus.PENDING); // Requires approval

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        logger.info("Restaurant created successfully with ID: {}", savedRestaurant.getId());
        return convertToRestaurantDTO(savedRestaurant);
    }

    /**
     * Get restaurant by ID with caching
     */
    @Cacheable(value = "restaurants", key = "#id")
    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurant(Long id) {
        logger.debug("Fetching restaurant with ID: {}", id);
        
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + id));
        
        return convertToRestaurantDTO(restaurant);
    }

    /**
     * Get all restaurants with pagination and filtering
     */
    @Cacheable(value = "restaurant-list", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<RestaurantDTO> getAllRestaurants(Pageable pageable) {
        logger.debug("Fetching all active restaurants, page: {}", pageable.getPageNumber());
        
        Page<Restaurant> restaurants = restaurantRepository.findByIsActiveTrueAndAcceptsOrdersTrue(pageable);
        return restaurants.map(this::convertToRestaurantDTO);
    }

    /**
     * Search restaurants with comprehensive filtering
     */
    @Transactional(readOnly = true)
    public Page<RestaurantDTO> searchRestaurants(String searchTerm, String cuisineType, 
                                                BigDecimal minRating, BigDecimal maxDeliveryFee,
                                                Double latitude, Double longitude,
                                                Pageable pageable) {
        logger.debug("Searching restaurants with term: {}, cuisine: {}, location: {},{}", 
                    searchTerm, cuisineType, latitude, longitude);

        Page<Restaurant> restaurants;

        // Location-based search if coordinates provided
        if (latitude != null && longitude != null) {
            restaurants = restaurantRepository.findRestaurantsWithinDeliveryRange(
                latitude, longitude, pageable);
        } 
        // Multi-criteria search
        else if (searchTerm != null || cuisineType != null || minRating != null || maxDeliveryFee != null) {
            restaurants = restaurantRepository.findByMultipleCriteria(
                cuisineType, minRating, maxDeliveryFee, searchTerm, pageable);
        }
        // Simple search by term
        else if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            restaurants = restaurantRepository.searchRestaurants(searchTerm.trim(), pageable);
        }
        // Default: all active restaurants
        else {
            restaurants = restaurantRepository.findByIsActiveTrueAndAcceptsOrdersTrue(pageable);
        }

        return restaurants.map(this::convertToRestaurantDTO);
    }

    /**
     * Get restaurants by cuisine type
     */
    @Cacheable(value = "restaurants-by-cuisine", key = "#cuisineType + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<RestaurantDTO> getRestaurantsByCuisine(String cuisineType, Pageable pageable) {
        logger.debug("Fetching restaurants by cuisine: {}", cuisineType);
        
        Page<Restaurant> restaurants = restaurantRepository
            .findByCuisineTypeIgnoreCaseAndIsActiveTrueAndAcceptsOrdersTrue(cuisineType, pageable);
        return restaurants.map(this::convertToRestaurantDTO);
    }

    /**
     * Get top rated restaurants
     */
    @Cacheable(value = "top-restaurants")
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getTopRatedRestaurants(int limit) {
        logger.debug("Fetching top {} rated restaurants", limit);
        
        List<Restaurant> restaurants = restaurantRepository.findTopRatedRestaurants(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "averageRating")));
        
        return restaurants.stream()
                         .map(this::convertToRestaurantDTO)
                         .collect(Collectors.toList());
    }

    /**
     * Get restaurants by owner ID
     */
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getOwnerRestaurants(Long ownerId) {
        logger.debug("Fetching restaurants for owner: {}", ownerId);
        
        List<Restaurant> restaurants = restaurantRepository.findAllByOwnerId(ownerId);
        return restaurants.stream()
                         .map(this::convertToRestaurantDTO)
                         .collect(Collectors.toList());
    }

    /**
     * Update restaurant
     */
    @CacheEvict(value = {"restaurants", "restaurant-list", "restaurants-by-cuisine"}, allEntries = true)
    public RestaurantDTO updateRestaurant(Long id, CreateRestaurantRequest request) {
        logger.info("Updating restaurant with ID: {}", id);

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + id));

        validateRestaurantRequest(request);

        // Update fields
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setAddress(request.getAddress());
        restaurant.setLatitude(request.getLatitude());
        restaurant.setLongitude(request.getLongitude());
        restaurant.setPhoneNumber(request.getPhoneNumber());
        restaurant.setEmail(request.getEmail());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setMinimumOrderAmount(request.getMinimumOrderAmount());
        restaurant.setDeliveryFee(request.getDeliveryFee());
        restaurant.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        restaurant.setLogoUrl(request.getLogoUrl());
        restaurant.setCoverImageUrl(request.getCoverImageUrl());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        logger.info("Restaurant updated successfully: {}", savedRestaurant.getId());
        return convertToRestaurantDTO(savedRestaurant);
    }

    /**
     * Update restaurant status (Admin operation)
     */
    @CacheEvict(value = {"restaurants", "restaurant-list"}, allEntries = true)
    public RestaurantDTO updateRestaurantStatus(Long id, RestaurantStatus status, String reason) {
        logger.info("Updating restaurant {} status to: {}", id, status);

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + id));

        restaurant.setStatus(status);
        
        // If rejecting or suspending, disable orders
        if (status == RestaurantStatus.REJECTED || status == RestaurantStatus.SUSPENDED) {
            restaurant.setAcceptsOrders(false);
        } else if (status == RestaurantStatus.APPROVED) {
            restaurant.setAcceptsOrders(true);
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        logger.info("Restaurant status updated successfully: {} -> {}", id, status);
        return convertToRestaurantDTO(savedRestaurant);
    }

    /**
     * Toggle restaurant availability
     */
    @CacheEvict(value = {"restaurants", "restaurant-list"}, allEntries = true)
    public RestaurantDTO toggleRestaurantAvailability(Long id, Boolean acceptsOrders) {
        logger.info("Toggling restaurant {} availability to: {}", id, acceptsOrders);

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + id));

        restaurant.setAcceptsOrders(acceptsOrders);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        return convertToRestaurantDTO(savedRestaurant);
    }

    // ==================== MENU MANAGEMENT ====================

    /**
     * Add menu item to restaurant
     */
    @CacheEvict(value = {"restaurant-menu", "menu-items"}, allEntries = true)
    public MenuItemDTO addMenuItem(Long restaurantId, CreateMenuItemRequest request) {
        logger.info("Adding menu item '{}' to restaurant {}", request.getName(), restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + restaurantId));

        // Validate menu item limit
        long currentItems = menuItemRepository.countByRestaurantIdAndIsAvailableTrue(restaurantId);
        if (currentItems >= 200) { // Max items per restaurant
            throw new IllegalStateException("Maximum number of menu items (200) reached for this restaurant");
        }

        // Check for duplicate item name
        if (menuItemRepository.existsByNameAndRestaurantId(request.getName(), restaurantId)) {
            throw new IllegalArgumentException("Menu item with this name already exists in this restaurant");
        }

        // Create menu item
        MenuItem menuItem = new MenuItem(request.getName(), request.getDescription(), 
                                       request.getPrice(), restaurant);

        // Set optional fields
        if (request.getImageUrl() != null) menuItem.setImageUrl(request.getImageUrl());
        if (request.getCategoryId() != null) {
            MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
            menuItem.setCategory(category);
        }

        menuItem.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        menuItem.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        menuItem.setIsVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false);
        menuItem.setIsVegan(request.getIsVegan() != null ? request.getIsVegan() : false);
        menuItem.setIsGlutenFree(request.getIsGlutenFree() != null ? request.getIsGlutenFree() : false);
        menuItem.setIsSpicy(request.getIsSpicy() != null ? request.getIsSpicy() : false);
        menuItem.setPreparationTime(request.getPreparationTime() != null ? request.getPreparationTime() : 15);
        menuItem.setCalories(request.getCalories());
        menuItem.setIngredients(request.getIngredients());
        menuItem.setAllergens(request.getAllergens());
        menuItem.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        
        logger.info("Menu item created successfully with ID: {}", savedMenuItem.getId());
        return convertToMenuItemDTO(savedMenuItem);
    }

    /**
     * Get restaurant menu with caching
     */
    @Cacheable(value = "restaurant-menu", key = "#restaurantId")
    @Transactional(readOnly = true)
    public Page<MenuItemDTO> getRestaurantMenu(Long restaurantId, Pageable pageable) {
        logger.debug("Fetching menu for restaurant: {}", restaurantId);
        
        Page<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndIsAvailableTrue(
            restaurantId, pageable);
        return menuItems.map(this::convertToMenuItemDTO);
    }

    /**
     * Get menu items by category
     */
    @Transactional(readOnly = true)
    public Page<MenuItemDTO> getMenuItemsByCategory(Long categoryId, Pageable pageable) {
        logger.debug("Fetching menu items for category: {}", categoryId);
        
        Page<MenuItem> menuItems = menuItemRepository.findByCategoryIdAndIsAvailableTrue(
            categoryId, pageable);
        return menuItems.map(this::convertToMenuItemDTO);
    }

    /**
     * Get featured menu items
     */
    @Cacheable(value = "featured-items", key = "#restaurantId")
    @Transactional(readOnly = true)
    public List<MenuItemDTO> getFeaturedMenuItems(Long restaurantId) {
        logger.debug("Fetching featured items for restaurant: {}", restaurantId);
        
        List<MenuItem> featuredItems = menuItemRepository
            .findByRestaurantIdAndIsFeaturedTrueAndIsAvailableTrue(restaurantId);
        
        return featuredItems.stream()
                           .map(this::convertToMenuItemDTO)
                           .collect(Collectors.toList());
    }

    /**
     * Search menu items
     */
    @Transactional(readOnly = true)
    public Page<MenuItemDTO> searchMenuItems(Long restaurantId, String searchTerm, Pageable pageable) {
        logger.debug("Searching menu items for restaurant {} with term: {}", restaurantId, searchTerm);
        
        Page<MenuItem> menuItems = menuItemRepository.searchMenuItems(restaurantId, searchTerm, pageable);
        return menuItems.map(this::convertToMenuItemDTO);
    }

    // ==================== CATEGORY MANAGEMENT ====================

    /**
     * Add menu category
     */
    @CacheEvict(value = {"menu-categories"}, allEntries = true)
    public MenuCategory addMenuCategory(Long restaurantId, CreateMenuCategoryRequest request) {
        logger.info("Adding category '{}' to restaurant {}", request.getName(), restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + restaurantId));

        // Check for duplicate category name
        if (menuCategoryRepository.existsByNameAndRestaurantId(request.getName(), restaurantId)) {
            throw new IllegalArgumentException("Category with this name already exists in this restaurant");
        }

        MenuCategory category = new MenuCategory(request.getName(), request.getDescription(), restaurant);
        category.setDisplayOrder(request.getDisplayOrder());
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(request.getIsActive());

        return menuCategoryRepository.save(category);
    }

    /**
     * Get restaurant categories
     */
    @Cacheable(value = "menu-categories", key = "#restaurantId")
    @Transactional(readOnly = true)
    public List<MenuCategory> getRestaurantCategories(Long restaurantId) {
        logger.debug("Fetching categories for restaurant: {}", restaurantId);
        
        return menuCategoryRepository.findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get available cuisine types
     */
    @Cacheable(value = "cuisine-types")
    @Transactional(readOnly = true)
    public List<String> getAvailableCuisineTypes() {
        logger.debug("Fetching available cuisine types");
        return restaurantRepository.findDistinctCuisineTypes();
    }

    /**
     * Get restaurants requiring approval (Admin)
     */
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getPendingRestaurants() {
        logger.debug("Fetching restaurants pending approval");
        
        List<Restaurant> pendingRestaurants = restaurantRepository
            .findByStatusOrderByCreatedAtAsc(RestaurantStatus.PENDING);
        
        return pendingRestaurants.stream()
                                .map(this::convertToRestaurantDTO)
                                .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate restaurant creation/update request
     */
    private void validateRestaurantRequest(CreateRestaurantRequest request) {
        // Validate operating hours
        if (request.getOpeningTime() != null && request.getClosingTime() != null) {
            // Allow same opening and closing time for 24-hour restaurants
            // but validate that they're both valid times
        }

        // Validate coordinates
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new IllegalArgumentException("Restaurant location coordinates are required");
        }

        // Validate business constraints
        if (request.getMinimumOrderAmount() != null && 
            request.getMinimumOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum order amount cannot be negative");
        }

        if (request.getDeliveryFee() != null && 
            request.getDeliveryFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Delivery fee cannot be negative");
        }
    }

    /**
     * Convert Restaurant entity to DTO
     */
    private RestaurantDTO convertToRestaurantDTO(Restaurant restaurant) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setDescription(restaurant.getDescription());
        dto.setCuisineType(restaurant.getCuisineType());
        dto.setAddress(restaurant.getAddress());
        dto.setLatitude(restaurant.getLatitude());
        dto.setLongitude(restaurant.getLongitude());
        dto.setPhoneNumber(restaurant.getPhoneNumber());
        dto.setEmail(restaurant.getEmail());
        dto.setOpeningTime(restaurant.getOpeningTime());
        dto.setClosingTime(restaurant.getClosingTime());
        dto.setOwnerId(restaurant.getOwnerId());
        dto.setStatus(restaurant.getStatus());
        dto.setIsActive(restaurant.getIsActive());
        dto.setAcceptsOrders(restaurant.getAcceptsOrders());
        dto.setAverageRating(restaurant.getAverageRating());
        dto.setTotalReviews(restaurant.getTotalReviews());
        dto.setMinimumOrderAmount(restaurant.getMinimumOrderAmount());
        dto.setDeliveryFee(restaurant.getDeliveryFee());
        dto.setEstimatedDeliveryTime(restaurant.getEstimatedDeliveryTime());
        dto.setLogoUrl(restaurant.getLogoUrl());
        dto.setCoverImageUrl(restaurant.getCoverImageUrl());
        dto.setCreatedAt(restaurant.getCreatedAt());
        dto.setUpdatedAt(restaurant.getUpdatedAt());

        // Set computed fields
        dto.setIsCurrentlyOpen(restaurant.isCurrentlyOpen());
        dto.setCanAcceptOrders(restaurant.canAcceptOrders());
        dto.setDeliveryRadiusKm(restaurant.getDeliveryRadiusKm());

        return dto;
    }

    /**
     * Convert MenuItem entity to DTO
     */
    private MenuItemDTO convertToMenuItemDTO(MenuItem menuItem) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setIsAvailable(menuItem.getIsAvailable());
        dto.setIsFeatured(menuItem.getIsFeatured());
        dto.setIsVegetarian(menuItem.getIsVegetarian());
        dto.setIsVegan(menuItem.getIsVegan());
        dto.setIsGlutenFree(menuItem.getIsGlutenFree());
        dto.setIsSpicy(menuItem.getIsSpicy());
        dto.setPreparationTime(menuItem.getPreparationTime());
        dto.setCalories(menuItem.getCalories());
        dto.setIngredients(menuItem.getIngredients());
        dto.setAllergens(menuItem.getAllergens());
        dto.setDisplayOrder(menuItem.getDisplayOrder());
        dto.setCreatedAt(menuItem.getCreatedAt());
        dto.setUpdatedAt(menuItem.getUpdatedAt());

        // Set restaurant information
        if (menuItem.getRestaurant() != null) {
            dto.setRestaurantId(menuItem.getRestaurant().getId());
            dto.setRestaurantName(menuItem.getRestaurant().getName());
        }

        // Set category information
        if (menuItem.getCategory() != null) {
            dto.setCategoryId(menuItem.getCategory().getId());
            dto.setCategoryName(menuItem.getCategory().getName());
        }

        // Set computed fields
        dto.setDietaryTags(menuItem.getDietaryTags());
        dto.setTotalPreparationTime(menuItem.getTotalPreparationTime());
        dto.setCanBeOrdered(menuItem.canBeOrdered());

        return dto;
    }
}
