package com.itcs383.restaurant.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

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
 * DTO for creating a new restaurant
 */
public class CreateRestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Restaurant name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Cuisine type is required")
    @Size(max = 50, message = "Cuisine type must not exceed 50 characters")
    private String cuisineType;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private Double longitude;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    private LocalTime closingTime;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotNull(message = "Minimum order amount is required")
    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    private BigDecimal minimumOrderAmount;

    @NotNull(message = "Delivery fee is required")
    @DecimalMin(value = "0.0", message = "Delivery fee cannot be negative")
    private BigDecimal deliveryFee;

    @NotNull(message = "Estimated delivery time is required")
    @Min(value = 1, message = "Estimated delivery time must be at least 1 minute")
    @Max(value = 180, message = "Estimated delivery time cannot exceed 180 minutes")
    private Integer estimatedDeliveryTime;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImageUrl;

    // Constructors
    public CreateRestaurantRequest() {}

    // Getters and Setters
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

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CreateRestaurantRequest request = new CreateRestaurantRequest();

        public Builder name(String name) { request.name = name; return this; }
        public Builder description(String description) { request.description = description; return this; }
        public Builder cuisineType(String cuisineType) { request.cuisineType = cuisineType; return this; }
        public Builder address(String address) { request.address = address; return this; }
        public Builder latitude(Double latitude) { request.latitude = latitude; return this; }
        public Builder longitude(Double longitude) { request.longitude = longitude; return this; }
        public Builder phoneNumber(String phoneNumber) { request.phoneNumber = phoneNumber; return this; }
        public Builder email(String email) { request.email = email; return this; }
        public Builder openingTime(LocalTime openingTime) { request.openingTime = openingTime; return this; }
        public Builder closingTime(LocalTime closingTime) { request.closingTime = closingTime; return this; }
        public Builder ownerId(Long ownerId) { request.ownerId = ownerId; return this; }
        public Builder minimumOrderAmount(BigDecimal minimumOrderAmount) { request.minimumOrderAmount = minimumOrderAmount; return this; }
        public Builder deliveryFee(BigDecimal deliveryFee) { request.deliveryFee = deliveryFee; return this; }
        public Builder estimatedDeliveryTime(Integer estimatedDeliveryTime) { request.estimatedDeliveryTime = estimatedDeliveryTime; return this; }
        public Builder logoUrl(String logoUrl) { request.logoUrl = logoUrl; return this; }
        public Builder coverImageUrl(String coverImageUrl) { request.coverImageUrl = coverImageUrl; return this; }

        public CreateRestaurantRequest build() { return request; }
    }
}
