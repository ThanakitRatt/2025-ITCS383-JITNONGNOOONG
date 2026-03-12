package com.itcs383.restaurant.service;

import com.itcs383.common.dto.MenuItemDTO;
import com.itcs383.common.dto.RestaurantDTO;
import com.itcs383.common.enums.RestaurantStatus;
import com.itcs383.common.exception.ResourceNotFoundException;
import com.itcs383.restaurant.dto.CreateMenuCategoryRequest;
import com.itcs383.restaurant.dto.CreateMenuItemRequest;
import com.itcs383.restaurant.dto.CreateRestaurantRequest;
import com.itcs383.restaurant.dto.UpdateMenuItemRequest;
import com.itcs383.restaurant.entity.MenuCategory;
import com.itcs383.restaurant.entity.MenuItem;
import com.itcs383.restaurant.entity.Restaurant;
import com.itcs383.restaurant.repository.MenuCategoryRepository;
import com.itcs383.restaurant.repository.MenuItemRepository;
import com.itcs383.restaurant.repository.RestaurantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    
    // Constants for validation and error messages
    private static final String RESOURCE_RESTAURANT = "Restaurant";
    private static final String RESOURCE_MENU_CATEGORY = "MenuCategory";
    private static final int MAX_MENU_ITEMS_PER_RESTAURANT = 200;
    private static final int DEFAULT_PREPARATION_TIME = 15;
    private static final int DEFAULT_DISPLAY_ORDER = 0;

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            MenuCategoryRepository menuCategoryRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.menuCategoryRepository = menuCategoryRepository;
    }

    // ==================== RESTAURANT MANAGEMENT ====================

    /**
     * Create new restaurant
     */
    public RestaurantDTO createRestaurant(@NonNull CreateRestaurantRequest request) {
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
            BigDecimal.valueOf(request.getLatitude()),
            BigDecimal.valueOf(request.getLongitude()),
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
    @SuppressWarnings("null")
    @Cacheable(value = "restaurants", key = "#id")
    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurant(Long id) {
        logger.debug("Fetching restaurant with ID: {}", id);
        
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_RESTAURANT, id));
        
        return convertToRestaurantDTO(restaurant);
    }

    /**
     * Get all restaurants with pagination and filtering
     * Note: @Cacheable removed - Page objects cannot be cached with Redis
     */
    @Transactional(readOnly = true)
    public Page<RestaurantDTO> getAllRestaurants(@NonNull Pageable pageable) {
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
                                                @NonNull Pageable pageable) {
        logger.debug("Searching restaurants with term: {}, cuisine: {}, location: {},{}", 
                    searchTerm, cuisineType, latitude, longitude);

        Page<Restaurant> restaurants;

        // Location-based search if coordinates provided
        if (latitude != null && longitude != null) {
            restaurants = restaurantRepository.findRestaurantsWithinDeliveryRange(
                latitude, longitude, pageable);
        } 
        // Simple search by term only (no other criteria)
        else if (searchTerm != null && !searchTerm.trim().isEmpty() && 
                 cuisineType == null && minRating == null && maxDeliveryFee == null) {
            restaurants = restaurantRepository.searchRestaurants(searchTerm.trim(), pageable);
        }
        // Multi-criteria search (includes searchTerm with other filters)
        else if (searchTerm != null || cuisineType != null || minRating != null || maxDeliveryFee != null) {
            restaurants = restaurantRepository.findByMultipleCriteria(
                cuisineType, minRating, maxDeliveryFee, searchTerm, pageable);
        }
        // Default: all active restaurants
        else {
            restaurants = restaurantRepository.findByIsActiveTrueAndAcceptsOrdersTrue(pageable);
        }

        return restaurants.map(this::convertToRestaurantDTO);
    }

    /**
     * Get restaurants by cuisine type
     * Note: @Cacheable removed - Page objects cannot be cached with Redis
     */
    @Transactional(readOnly = true)
    public Page<RestaurantDTO> getRestaurantsByCuisine(@NonNull String cuisineType, @NonNull Pageable pageable) {
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
                         .toList();
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
                         .toList();
    }

    /**
     * Update restaurant
     */
    @CacheEvict(value = {"restaurants", "restaurant-list", "restaurants-by-cuisine"}, allEntries = true)
    public RestaurantDTO updateRestaurant(@NonNull Long id, @NonNull CreateRestaurantRequest request) {
        logger.info("Updating restaurant with ID: {}", id);

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_RESTAURANT, id));

        validateRestaurantRequest(request);

        // Update fields
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setAddress(request.getAddress());
        restaurant.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        restaurant.setLongitude(BigDecimal.valueOf(request.getLongitude()));
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
    public RestaurantDTO updateRestaurantStatus(@NonNull Long id, @NonNull RestaurantStatus status, String reason) {
        logger.info("Updating restaurant {} status to: {}", id, status);

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_RESTAURANT, id));

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
    public RestaurantDTO toggleRestaurantAvailability(@NonNull Long id, @NonNull Boolean acceptsOrders) {
        logger.info("Toggling restaurant {} availability to: {}", id, acceptsOrders);

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_RESTAURANT, id));

        restaurant.setAcceptsOrders(acceptsOrders);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        
        return convertToRestaurantDTO(savedRestaurant);
    }

    // ==================== MENU MANAGEMENT ====================

    /**
     * Add menu item to restaurant
     */
    @SuppressWarnings("null")
    @CacheEvict(value = {"restaurant-menu", "menu-items"}, allEntries = true)
    public MenuItemDTO addMenuItem(@NonNull Long restaurantId, @NonNull CreateMenuItemRequest request) {
        logger.info("Adding menu item '{}' to restaurant {}", request.getName(), restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_RESTAURANT, restaurantId));

        // Validate menu item limit
        long currentItems = menuItemRepository.countByRestaurantIdAndIsAvailableTrue(restaurantId);
        if (currentItems >= MAX_MENU_ITEMS_PER_RESTAURANT) {
            throw new IllegalStateException("Maximum number of menu items (" + MAX_MENU_ITEMS_PER_RESTAURANT + ") reached for this restaurant");
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
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_MENU_CATEGORY, request.getCategoryId()));
            menuItem.setCategory(category);
        }

        menuItem.setIsAvailable(Optional.ofNullable(request.getIsAvailable()).orElse(Boolean.TRUE));
        menuItem.setIsFeatured(Optional.ofNullable(request.getIsFeatured()).orElse(Boolean.FALSE));
        menuItem.setIsVegetarian(Optional.ofNullable(request.getIsVegetarian()).orElse(Boolean.FALSE));
        menuItem.setIsVegan(Optional.ofNullable(request.getIsVegan()).orElse(Boolean.FALSE));
        menuItem.setIsGlutenFree(Optional.ofNullable(request.getIsGlutenFree()).orElse(Boolean.FALSE));
        menuItem.setIsSpicy(Optional.ofNullable(request.getIsSpicy()).orElse(Boolean.FALSE));
        menuItem.setPreparationTime(request.getPreparationTime() != null ? request.getPreparationTime() : DEFAULT_PREPARATION_TIME);
        menuItem.setCalories(request.getCalories());
        menuItem.setIngredients(request.getIngredients());
        menuItem.setAllergens(request.getAllergens());
        menuItem.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : DEFAULT_DISPLAY_ORDER);

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        
        logger.info("Menu item created successfully with ID: {}", savedMenuItem.getId());
        return convertToMenuItemDTO(savedMenuItem);
    }

    /**
     * Get restaurant menu with caching
     */
    @Transactional(readOnly = true)
    public Page<MenuItemDTO> getRestaurantMenu(@NonNull Long restaurantId, @NonNull Pageable pageable) {
        logger.debug("Fetching menu for restaurant: {}", restaurantId);
        
        Page<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndIsAvailableTrue(
            restaurantId, pageable);
        return menuItems.map(this::convertToMenuItemDTO);
    }

    /**
     * Get menu items by category
     */
    @Transactional(readOnly = true)
    public Page<MenuItemDTO> getMenuItemsByCategory(@NonNull Long categoryId, @NonNull Pageable pageable) {
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
                           .toList();
    }

    /**
     * Search menu items
     */
    @Transactional(readOnly = true)
    public Page<MenuItemDTO> searchMenuItems(@NonNull Long restaurantId, @NonNull String searchTerm, @NonNull Pageable pageable) {
        logger.debug("Searching menu items for restaurant {} with term: {}", restaurantId, searchTerm);
        
        Page<MenuItem> menuItems = menuItemRepository.searchMenuItems(restaurantId, searchTerm, pageable);
        return menuItems.map(this::convertToMenuItemDTO);
    }

    /**
     * Update menu item
     */
    @CacheEvict(value = {"restaurant-menu", "menu-items", "featured-items"}, allEntries = true)
    public MenuItemDTO updateMenuItem(@NonNull Long restaurantId, @NonNull Long itemId,
                                     @NonNull UpdateMenuItemRequest request) {
        logger.info("Updating menu item {} in restaurant {}", itemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));

        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Menu item does not belong to this restaurant");
        }

        applyMenuItemUpdates(menuItem, request);

        MenuItem saved = menuItemRepository.save(menuItem);
        logger.info("Menu item {} updated successfully", itemId);
        return convertToMenuItemDTO(saved);
    }

    private void applyMenuItemUpdates(MenuItem menuItem, UpdateMenuItemRequest request) {
        if (request.getName() != null) menuItem.setName(request.getName());
        if (request.getDescription() != null) menuItem.setDescription(request.getDescription());
        if (request.getPrice() != null) menuItem.setPrice(request.getPrice());
        if (request.getImageUrl() != null) menuItem.setImageUrl(request.getImageUrl());
        if (request.getIsAvailable() != null) menuItem.setIsAvailable(request.getIsAvailable());
        if (request.getIsFeatured() != null) menuItem.setIsFeatured(request.getIsFeatured());
        if (request.getIsVegetarian() != null) menuItem.setIsVegetarian(request.getIsVegetarian());
        if (request.getIsVegan() != null) menuItem.setIsVegan(request.getIsVegan());
        if (request.getIsGlutenFree() != null) menuItem.setIsGlutenFree(request.getIsGlutenFree());
        if (request.getIsSpicy() != null) menuItem.setIsSpicy(request.getIsSpicy());
        if (request.getPreparationTime() != null) menuItem.setPreparationTime(request.getPreparationTime());
        if (request.getCalories() != null) menuItem.setCalories(request.getCalories());
        if (request.getIngredients() != null) menuItem.setIngredients(request.getIngredients());
        if (request.getAllergens() != null) menuItem.setAllergens(request.getAllergens());
        if (request.getDisplayOrder() != null) menuItem.setDisplayOrder(request.getDisplayOrder());
        applyMenuItemCategory(menuItem, request.getCategoryId());
    }

    private void applyMenuItemCategory(MenuItem menuItem, Long categoryId) {
        if (categoryId == null) return;
        MenuCategory category = menuCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_MENU_CATEGORY, categoryId));
        menuItem.setCategory(category);
    }

    /**
     * Delete menu item
     */
    @CacheEvict(value = {"restaurant-menu", "menu-items", "featured-items"}, allEntries = true)
    public void deleteMenuItem(@NonNull Long restaurantId, @NonNull Long itemId) {
        logger.info("Deleting menu item {} from restaurant {}", itemId, restaurantId);

        MenuItem menuItem = menuItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));

        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Menu item does not belong to this restaurant");
        }

        menuItemRepository.delete(menuItem);
        logger.info("Menu item {} deleted successfully", itemId);
    }

    /**
     * Delete restaurant
     */
    @CacheEvict(value = {"restaurants", "restaurant-list", "restaurant-menu", "menu-categories"}, allEntries = true)
    public void deleteRestaurant(@NonNull Long id) {
        logger.info("Deleting restaurant {}", id);

        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_RESTAURANT, id));

        restaurantRepository.delete(restaurant);
        logger.info("Restaurant {} deleted successfully", id);
    }

    /**
     * Delete menu category
     */
    @CacheEvict(value = {"menu-categories"}, allEntries = true)
    public void deleteMenuCategory(@NonNull Long restaurantId, @NonNull Long categoryId) {
        logger.info("Deleting category {} from restaurant {}", categoryId, restaurantId);

        MenuCategory category = menuCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_MENU_CATEGORY, categoryId));

        if (!category.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Category does not belong to this restaurant");
        }

        menuCategoryRepository.delete(category);
        logger.info("Category {} deleted successfully", categoryId);
    }

    // ==================== CATEGORY MANAGEMENT ====================

    /**
     * Add menu category
     */
    @CacheEvict(value = {"menu-categories"}, allEntries = true)
    public MenuCategory addMenuCategory(@NonNull Long restaurantId, @NonNull CreateMenuCategoryRequest request) {
        logger.info("Adding category '{}' to restaurant {}", request.getName(), restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_RESTAURANT, restaurantId));

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
    // @Cacheable(value = "menu-categories", key = "#restaurantId") // Temporarily disabled due to lazy initialization with Redis
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
                                .toList();
    }

    /**
     * Get admin stats: total and active restaurant counts
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getRestaurantStats() {
        long totalRestaurants = restaurantRepository.count();
        long activeRestaurants = restaurantRepository.countByStatus(RestaurantStatus.APPROVED);
        return java.util.Map.of(
            "totalRestaurants", totalRestaurants,
            "activeRestaurants", activeRestaurants
        );
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate restaurant creation/update request
     */
    private void validateRestaurantRequest(@NonNull CreateRestaurantRequest request) {
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
    private RestaurantDTO convertToRestaurantDTO(@NonNull Restaurant restaurant) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setDescription(restaurant.getDescription());
        dto.setCuisineType(restaurant.getCuisineType());
        dto.setAddress(restaurant.getAddress());
        dto.setLatitude(restaurant.getLatitude().doubleValue());
        dto.setLongitude(restaurant.getLongitude().doubleValue());
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
    private MenuItemDTO convertToMenuItemDTO(@NonNull MenuItem menuItem) {
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
        String tags = menuItem.getDietaryTags();
        dto.setDietaryTags(tags != null && !tags.isEmpty() ? 
            java.util.Arrays.asList(tags.split(",\\s*")) : 
            java.util.Collections.emptyList());
        dto.setTotalPreparationTime(menuItem.getTotalPreparationTime());
        dto.setCanBeOrdered(menuItem.canBeOrdered());

        return dto;
    }
}
