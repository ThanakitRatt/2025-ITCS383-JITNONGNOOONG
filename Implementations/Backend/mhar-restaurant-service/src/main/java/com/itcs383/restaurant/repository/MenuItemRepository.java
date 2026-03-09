package com.itcs383.restaurant.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.itcs383.restaurant.entity.MenuItem;

/**
 * MenuItem Repository
 * 
 * Data access layer for MenuItem entities
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Find all menu items for a restaurant
     */
    Page<MenuItem> findByRestaurantId(Long restaurantId, Pageable pageable);

    /**
     * Find available menu items for a restaurant
     */
    Page<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId, Pageable pageable);

    /**
     * Find menu items by category
     */
    Page<MenuItem> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find available menu items by category
     */
    Page<MenuItem> findByCategoryIdAndIsAvailableTrue(Long categoryId, Pageable pageable);

    /**
     * Find featured menu items for a restaurant
     */
    List<MenuItem> findByRestaurantIdAndIsFeaturedTrueAndIsAvailableTrue(Long restaurantId);

    /**
     * Search menu items by name or description
     */
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND " +
           "m.isAvailable = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<MenuItem> searchMenuItems(
        @Param("restaurantId") Long restaurantId, 
        @Param("searchTerm") String searchTerm, 
        Pageable pageable);

    /**
     * Find menu items by price range
     */
    Page<MenuItem> findByRestaurantIdAndIsAvailableTrueAndPriceBetween(
        Long restaurantId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find vegetarian menu items
     */
    Page<MenuItem> findByRestaurantIdAndIsAvailableTrueAndIsVegetarianTrue(
        Long restaurantId, Pageable pageable);

    /**
     * Find vegan menu items
     */
    Page<MenuItem> findByRestaurantIdAndIsAvailableTrueAndIsVeganTrue(
        Long restaurantId, Pageable pageable);

    /**
     * Find gluten-free menu items
     */
    Page<MenuItem> findByRestaurantIdAndIsAvailableTrueAndIsGlutenFreeTrue(
        Long restaurantId, Pageable pageable);

    /**
     * Find menu items by dietary preferences
     */
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND " +
           "m.isAvailable = true AND " +
           "(:vegetarian = false OR m.isVegetarian = true) AND " +
           "(:vegan = false OR m.isVegan = true) AND " +
           "(:glutenFree = false OR m.isGlutenFree = true)")
    Page<MenuItem> findByDietaryPreferences(
        @Param("restaurantId") Long restaurantId,
        @Param("vegetarian") boolean vegetarian,
        @Param("vegan") boolean vegan,
        @Param("glutenFree") boolean glutenFree,
        Pageable pageable);

    /**
     * Get menu items ordered by display order
     */
    List<MenuItem> findByRestaurantIdAndIsAvailableTrueOrderByDisplayOrderAsc(Long restaurantId);

    /**
     * Get menu items by category ordered by display order
     */
    List<MenuItem> findByCategoryIdAndIsAvailableTrueOrderByDisplayOrderAsc(Long categoryId);

    /**
     * Count available menu items for restaurant
     */
    long countByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    /**
     * Count menu items by category
     */
    long countByCategoryIdAndIsAvailableTrue(Long categoryId);

    /**
     * Find cheapest menu items for a restaurant
     */
    List<MenuItem> findTop5ByRestaurantIdAndIsAvailableTrueOrderByPriceAsc(Long restaurantId);

    /**
     * Find most expensive menu items for a restaurant
     */
    List<MenuItem> findTop5ByRestaurantIdAndIsAvailableTrueOrderByPriceDesc(Long restaurantId);

    /**
     * Get average price of menu items for restaurant
     */
    @Query("SELECT AVG(m.price) FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.isAvailable = true")
    BigDecimal getAveragePriceByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * Check if menu item name exists for restaurant (for validation)
     */
    boolean existsByNameAndRestaurantId(String name, Long restaurantId);

    /**
     * Find items with similar prices (for recommendations)
     */
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND " +
           "m.isAvailable = true AND m.id != :excludeItemId AND " +
           "m.price BETWEEN :minPrice AND :maxPrice")
    List<MenuItem> findSimilarPricedItems(
        @Param("restaurantId") Long restaurantId,
        @Param("excludeItemId") Long excludeItemId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice);
}
