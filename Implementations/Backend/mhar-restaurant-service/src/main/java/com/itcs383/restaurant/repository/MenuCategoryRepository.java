package com.itcs383.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.itcs383.restaurant.entity.MenuCategory;

/**
 * MenuCategory Repository
 * 
 * Data access layer for MenuCategory entities
 */
@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    /**
     * Find all categories for a restaurant
     */
    List<MenuCategory> findByRestaurantId(Long restaurantId);

    /**
     * Find active categories for a restaurant ordered by display order
     */
    List<MenuCategory> findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(Long restaurantId);

    /**
     * Find category by name and restaurant
     */
    Optional<MenuCategory> findByNameAndRestaurantId(String name, Long restaurantId);

    /**
     * Check if category name exists for restaurant (for validation)
     */
    boolean existsByNameAndRestaurantId(String name, Long restaurantId);

    /**
     * Count active categories for restaurant
     */
    long countByRestaurantIdAndIsActiveTrue(Long restaurantId);

    /**
     * Find categories with available items
     */
    @Query("SELECT DISTINCT c FROM MenuCategory c " +
           "JOIN c.menuItems m WHERE c.restaurant.id = :restaurantId AND " +
           "c.isActive = true AND m.isAvailable = true " +
           "ORDER BY c.displayOrder ASC")
    List<MenuCategory> findCategoriesWithAvailableItems(@Param("restaurantId") Long restaurantId);

    /**
     * Get category with item count
     */
    @Query("SELECT c, COUNT(m) FROM MenuCategory c " +
           "LEFT JOIN c.menuItems m ON m.isAvailable = true " +
           "WHERE c.restaurant.id = :restaurantId AND c.isActive = true " +
           "GROUP BY c ORDER BY c.displayOrder ASC")
    List<Object[]> findCategoriesWithItemCount(@Param("restaurantId") Long restaurantId);
}
