package com.itcs383.restaurant.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.itcs383.common.enums.RestaurantStatus;
import com.itcs383.restaurant.entity.Restaurant;

/**
 * Restaurant Repository
 * 
 * Data access layer for Restaurant entities with advanced query methods
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Find restaurant by owner ID
     */
    Optional<Restaurant> findByOwnerId(Long ownerId);

    /**
     * Find all restaurants by owner ID
     */
    List<Restaurant> findAllByOwnerId(Long ownerId);

    /**
     * Find restaurants by status
     */
    Page<Restaurant> findByStatus(RestaurantStatus status, Pageable pageable);

    /**
     * Find active restaurants that accept orders
     */
    Page<Restaurant> findByIsActiveTrueAndAcceptsOrdersTrue(Pageable pageable);

    /**
     * Find restaurants by cuisine type
     */
    Page<Restaurant> findByCuisineTypeIgnoreCaseAndIsActiveTrueAndAcceptsOrdersTrue(
        String cuisineType, Pageable pageable);

    /**
     * Search restaurants by name or cuisine type
     */
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.acceptsOrders = true AND " +
           "(LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Restaurant> searchRestaurants(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find restaurants within delivery radius of given coordinates
     */
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.acceptsOrders = true AND " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(r.latitude)))) <= r.estimatedDeliveryTime / 2.0")
    Page<Restaurant> findRestaurantsWithinDeliveryRange(
        @Param("latitude") Double latitude, 
        @Param("longitude") Double longitude, 
        Pageable pageable);

    /**
     * Find restaurants by minimum rating
     */
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.acceptsOrders = true AND " +
           "r.averageRating >= :minRating ORDER BY r.averageRating DESC")
    Page<Restaurant> findByMinimumRating(@Param("minRating") BigDecimal minRating, Pageable pageable);

    /**
     * Find restaurants with delivery fee less than or equal to specified amount
     */
    Page<Restaurant> findByIsActiveTrueAndAcceptsOrdersTrueAndDeliveryFeeLessThanEqual(
        BigDecimal maxDeliveryFee, Pageable pageable);

    /**
     * Get top rated restaurants
     */
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.acceptsOrders = true AND " +
           "r.totalReviews >= 5 ORDER BY r.averageRating DESC")
    List<Restaurant> findTopRatedRestaurants(Pageable pageable);

    /**
     * Get restaurants by multiple criteria
     */
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.acceptsOrders = true AND " +
           "(:cuisineType IS NULL OR LOWER(r.cuisineType) = LOWER(:cuisineType)) AND " +
           "(:minRating IS NULL OR r.averageRating >= :minRating) AND " +
           "(:maxDeliveryFee IS NULL OR r.deliveryFee <= :maxDeliveryFee) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Restaurant> findByMultipleCriteria(
        @Param("cuisineType") String cuisineType,
        @Param("minRating") BigDecimal minRating,
        @Param("maxDeliveryFee") BigDecimal maxDeliveryFee,
        @Param("searchTerm") String searchTerm,
        Pageable pageable);

    /**
     * Find restaurants currently open (this would need to be handled by service layer
     * as JPA queries can't easily handle time comparisons)
     */
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.acceptsOrders = true")
    List<Restaurant> findAllActiveRestaurants();

    /**
     * Get restaurant statistics
     */
    @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.status = :status")
    Long countByStatus(@Param("status") RestaurantStatus status);

    /**
     * Get restaurants pending approval
     */
    List<Restaurant> findByStatusOrderByCreatedAtAsc(RestaurantStatus status);

    /**
     * Check if restaurant name exists for owner (for validation)
     */
    boolean existsByNameAndOwnerId(String name, Long ownerId);

    /**
     * Find restaurants with recent updates (last 24 hours)
     */
    @Query("SELECT r FROM Restaurant r WHERE r.updatedAt >= :since")
    List<Restaurant> findRecentlyUpdatedRestaurants(@Param("since") java.time.LocalDateTime since);

    /**
     * Get distinct cuisine types
     */
    @Query("SELECT DISTINCT r.cuisineType FROM Restaurant r WHERE r.isActive = true ORDER BY r.cuisineType")
    List<String> findDistinctCuisineTypes();
}
