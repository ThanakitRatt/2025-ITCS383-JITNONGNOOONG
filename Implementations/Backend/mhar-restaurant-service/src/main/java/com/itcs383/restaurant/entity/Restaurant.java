package com.itcs383.restaurant.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.itcs383.common.enums.RestaurantStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Restaurant Entity
 * 
 * Represents a restaurant in the food delivery platform
 * Contains restaurant information, operating hours, location, and business metrics
 */
@Entity
@Table(name = "restaurants", indexes = {
    @Index(name = "idx_restaurant_status", columnList = "status"),
    @Index(name = "idx_restaurant_cuisine", columnList = "cuisineType"),
    @Index(name = "idx_restaurant_location", columnList = "latitude, longitude"),
    @Index(name = "idx_restaurant_rating", columnList = "averageRating"),
    @Index(name = "idx_restaurant_active", columnList = "isActive")
})
@EntityListeners(AuditingEntityListener.class)
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Restaurant name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotBlank(message = "Cuisine type is required")
    @Size(max = 50, message = "Cuisine type must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String cuisineType;

    // Address Information
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(nullable = false)
    private String address;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    @Column(nullable = false, precision = 10, scale = 8)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    @Column(nullable = false, precision = 11, scale = 8)
    private Double longitude;

    // Contact Information
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]{10,15}$", message = "Invalid phone number format")
    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(length = 100)
    private String email;

    // Operating Hours
    @NotNull(message = "Opening time is required")
    @Column(nullable = false)
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    @Column(nullable = false)
    private LocalTime closingTime;

    // Business Information
    @NotNull(message = "Owner ID is required")
    @Column(nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RestaurantStatus status = RestaurantStatus.PENDING;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean acceptsOrders = true;

    // Ratings and Reviews
    @DecimalMin(value = "0.0", message = "Average rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Average rating cannot exceed 5.0")
    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Min(value = 0, message = "Total reviews cannot be negative")
    @Column(nullable = false)
    private Integer totalReviews = 0;

    // Delivery Information
    @NotNull(message = "Minimum order amount is required")
    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount;

    @NotNull(message = "Delivery fee is required")
    @DecimalMin(value = "0.0", message = "Delivery fee cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @NotNull(message = "Estimated delivery time is required")
    @Min(value = 1, message = "Estimated delivery time must be at least 1 minute")
    @Max(value = 180, message = "Estimated delivery time cannot exceed 180 minutes")
    @Column(nullable = false)
    private Integer estimatedDeliveryTime; // in minutes

    // Image URLs
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    @Column(length = 500)
    private String logoUrl;

    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    @Column(length = 500)
    private String coverImageUrl;

    // Audit fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuCategory> menuCategories = new ArrayList<>();

    // Constructors
    public Restaurant() {}

    public Restaurant(String name, String description, String cuisineType, String address, 
                     Double latitude, Double longitude, String phoneNumber, 
                     LocalTime openingTime, LocalTime closingTime, Long ownerId,
                     BigDecimal minimumOrderAmount, BigDecimal deliveryFee, 
                     Integer estimatedDeliveryTime) {
        this.name = name;
        this.description = description;
        this.cuisineType = cuisineType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.ownerId = ownerId;
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryFee = deliveryFee;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    // Business Logic Methods
    
    /**
     * Check if restaurant is currently open
     */
    public boolean isCurrentlyOpen() {
        if (!isActive || !acceptsOrders) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        
        // Handle cases where restaurant operates past midnight
        if (closingTime.isBefore(openingTime)) {
            return !now.isBefore(openingTime) || !now.isAfter(closingTime);
        }
        
        return !now.isBefore(openingTime) && !now.isAfter(closingTime);
    }

    /**
     * Check if restaurant can accept orders
     */
    public boolean canAcceptOrders() {
        return isActive && acceptsOrders && 
               status == RestaurantStatus.APPROVED && 
               isCurrentlyOpen();
    }

    /**
     * Update average rating with new rating
     */
    public void updateRating(BigDecimal newRating) {
        if (newRating == null || newRating.compareTo(BigDecimal.ZERO) < 0 || 
            newRating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        BigDecimal totalScore = averageRating.multiply(BigDecimal.valueOf(totalReviews))
                                            .add(newRating);
        totalReviews++;
        averageRating = totalScore.divide(BigDecimal.valueOf(totalReviews), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate delivery radius based on delivery time (approximate)
     */
    public double getDeliveryRadiusKm() {
        // Assuming average speed of 30 km/h
        return (estimatedDeliveryTime / 60.0) * 30.0;
    }

    /**
     * Check if restaurant delivers to given location
     */
    public boolean deliversTo(double targetLatitude, double targetLongitude) {
        double distance = calculateDistance(latitude, longitude, targetLatitude, targetLongitude);
        return distance <= getDeliveryRadiusKm();
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalTime getOpeningTime() { return openingTime; }
    public void setOpeningTime(LocalTime openingTime) { this.openingTime = openingTime; }

    public LocalTime getClosingTime() { return closingTime; }
    public void setClosingTime(LocalTime closingTime) { this.closingTime = closingTime; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public RestaurantStatus getStatus() { return status; }
    public void setStatus(RestaurantStatus status) { this.status = status; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getAcceptsOrders() { return acceptsOrders; }
    public void setAcceptsOrders(Boolean acceptsOrders) { this.acceptsOrders = acceptsOrders; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) { this.minimumOrderAmount = minimumOrderAmount; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public Integer getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(Integer estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<MenuItem> getMenuItems() { return menuItems; }
    public void setMenuItems(List<MenuItem> menuItems) { this.menuItems = menuItems; }

    public List<MenuCategory> getMenuCategories() { return menuCategories; }
    public void setMenuCategories(List<MenuCategory> menuCategories) { this.menuCategories = menuCategories; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cuisineType='" + cuisineType + '\'' +
                ", address='" + address + '\'' +
                ", status=" + status +
                ", isActive=" + isActive +
                '}';
    }
}
