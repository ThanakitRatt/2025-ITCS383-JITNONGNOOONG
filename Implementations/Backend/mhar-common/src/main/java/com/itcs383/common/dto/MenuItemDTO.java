package com.itcs383.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Menu Item Data Transfer Object
 * 
 * Used for API responses and inter-service communication
 */
public class MenuItemDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isFeatured;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isGlutenFree;
    private Boolean isSpicy;
    private Integer preparationTime;
    private Integer calories;
    private String ingredients;
    private String allergens;
    private Integer displayOrder;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Restaurant information
    private Long restaurantId;
    private String restaurantName;

    // Category information
    private Long categoryId;
    private String categoryName;

    // Computed fields
    private List<String> dietaryTags;
    private Integer totalPreparationTime;
    private Boolean canBeOrdered;

    // Constructors
    public MenuItemDTO() {}

    public MenuItemDTO(Long id, String name, String description, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public Boolean getIsVegetarian() { return isVegetarian; }
    public void setIsVegetarian(Boolean isVegetarian) { this.isVegetarian = isVegetarian; }

    public Boolean getIsVegan() { return isVegan; }
    public void setIsVegan(Boolean isVegan) { this.isVegan = isVegan; }

    public Boolean getIsGlutenFree() { return isGlutenFree; }
    public void setIsGlutenFree(Boolean isGlutenFree) { this.isGlutenFree = isGlutenFree; }

    public Boolean getIsSpicy() { return isSpicy; }
    public void setIsSpicy(Boolean isSpicy) { this.isSpicy = isSpicy; }

    public Integer getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }

    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<String> getDietaryTags() { return dietaryTags; }
    public void setDietaryTags(List<String> dietaryTags) { this.dietaryTags = dietaryTags; }

    public Integer getTotalPreparationTime() { return totalPreparationTime; }
    public void setTotalPreparationTime(Integer totalPreparationTime) { this.totalPreparationTime = totalPreparationTime; }

    public Boolean getCanBeOrdered() { return canBeOrdered; }
    public void setCanBeOrdered(Boolean canBeOrdered) { this.canBeOrdered = canBeOrdered; }

    @Override
    public String toString() {
        return "MenuItemDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", restaurantId=" + restaurantId +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
