package com.itcs383.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.itcs383.common.enums.RestaurantStatus;

/**
 * Restaurant Data Transfer Object
 * 
 * Used for API responses and inter-service communication
 */
public class RestaurantDTO {

    private Long id;
    private String name;
    private String description;
    private String cuisineType;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private String email;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Long ownerId;
    private RestaurantStatus status;
    private Boolean isActive;
    private Boolean acceptsOrders;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    private String logoUrl;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional computed fields
    private Boolean isCurrentlyOpen;
    private Boolean canAcceptOrders;
    private Double deliveryRadiusKm;

    // Constructors
    public RestaurantDTO() {}

    public RestaurantDTO(Long id, String name, String cuisineType, String address, 
                        RestaurantStatus status, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.cuisineType = cuisineType;
        this.address = address;
        this.status = status;
        this.isActive = isActive;
    }

    // Full constructor
    public RestaurantDTO(Long id, String name, String description, String cuisineType,
                        String address, Double latitude, Double longitude, String phoneNumber,
                        LocalTime openingTime, LocalTime closingTime, Long ownerId,
                        RestaurantStatus status, Boolean isActive, Boolean acceptsOrders,
                        BigDecimal averageRating, Integer totalReviews, BigDecimal minimumOrderAmount,
                        BigDecimal deliveryFee, Integer estimatedDeliveryTime) {
        this.id = id;
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
        this.status = status;
        this.isActive = isActive;
        this.acceptsOrders = acceptsOrders;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryFee = deliveryFee;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
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

    public Boolean getIsCurrentlyOpen() { return isCurrentlyOpen; }
    public void setIsCurrentlyOpen(Boolean isCurrentlyOpen) { this.isCurrentlyOpen = isCurrentlyOpen; }

    public Boolean getCanAcceptOrders() { return canAcceptOrders; }
    public void setCanAcceptOrders(Boolean canAcceptOrders) { this.canAcceptOrders = canAcceptOrders; }

    public Double getDeliveryRadiusKm() { return deliveryRadiusKm; }
    public void setDeliveryRadiusKm(Double deliveryRadiusKm) { this.deliveryRadiusKm = deliveryRadiusKm; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RestaurantDTO dto = new RestaurantDTO();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder cuisineType(String cuisineType) { dto.cuisineType = cuisineType; return this; }
        public Builder address(String address) { dto.address = address; return this; }
        public Builder latitude(Double latitude) { dto.latitude = latitude; return this; }
        public Builder longitude(Double longitude) { dto.longitude = longitude; return this; }
        public Builder phoneNumber(String phoneNumber) { dto.phoneNumber = phoneNumber; return this; }
        public Builder email(String email) { dto.email = email; return this; }
        public Builder openingTime(LocalTime openingTime) { dto.openingTime = openingTime; return this; }
        public Builder closingTime(LocalTime closingTime) { dto.closingTime = closingTime; return this; }
        public Builder ownerId(Long ownerId) { dto.ownerId = ownerId; return this; }
        public Builder status(RestaurantStatus status) { dto.status = status; return this; }
        public Builder isActive(Boolean isActive) { dto.isActive = isActive; return this; }
        public Builder acceptsOrders(Boolean acceptsOrders) { dto.acceptsOrders = acceptsOrders; return this; }
        public Builder averageRating(BigDecimal averageRating) { dto.averageRating = averageRating; return this; }
        public Builder totalReviews(Integer totalReviews) { dto.totalReviews = totalReviews; return this; }
        public Builder minimumOrderAmount(BigDecimal minimumOrderAmount) { dto.minimumOrderAmount = minimumOrderAmount; return this; }
        public Builder deliveryFee(BigDecimal deliveryFee) { dto.deliveryFee = deliveryFee; return this; }
        public Builder estimatedDeliveryTime(Integer estimatedDeliveryTime) { dto.estimatedDeliveryTime = estimatedDeliveryTime; return this; }
        public Builder logoUrl(String logoUrl) { dto.logoUrl = logoUrl; return this; }
        public Builder coverImageUrl(String coverImageUrl) { dto.coverImageUrl = coverImageUrl; return this; }
        public Builder isCurrentlyOpen(Boolean isCurrentlyOpen) { dto.isCurrentlyOpen = isCurrentlyOpen; return this; }
        public Builder canAcceptOrders(Boolean canAcceptOrders) { dto.canAcceptOrders = canAcceptOrders; return this; }
        public Builder deliveryRadiusKm(Double deliveryRadiusKm) { dto.deliveryRadiusKm = deliveryRadiusKm; return this; }

        public RestaurantDTO build() { return dto; }
    }

    @Override
    public String toString() {
        return "RestaurantDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cuisineType='" + cuisineType + '\'' +
                ", status=" + status +
                ", isActive=" + isActive +
                '}';
    }
}
