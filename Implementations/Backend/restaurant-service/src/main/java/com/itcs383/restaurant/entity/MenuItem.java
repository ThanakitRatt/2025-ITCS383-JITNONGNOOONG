package com.itcs383.restaurant.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Menu Item Entity
 * 
 * Represents individual menu items available at restaurants
 */
@Entity
@Table(name = "menu_items", indexes = {
    @Index(name = "idx_menu_item_restaurant", columnList = "restaurant_id"),
    @Index(name = "idx_menu_item_category", columnList = "category_id"),
    @Index(name = "idx_menu_item_available", columnList = "isAvailable"),
    @Index(name = "idx_menu_item_featured", columnList = "isFeatured"),
    @Index(name = "idx_menu_item_price", columnList = "price")
})
@EntityListeners(AuditingEntityListener.class)
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Menu item name is required")
    @Size(min = 2, max = 100, message = "Menu item name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    @Column(nullable = false)
    private Boolean isFeatured = false;

    @Column(nullable = false)
    private Boolean isVegetarian = false;

    @Column(nullable = false)
    private Boolean isVegan = false;

    @Column(nullable = false)
    private Boolean isGlutenFree = false;

    @Column(nullable = false)
    private Boolean isSpicy = false;

    @Min(value = 0, message = "Preparation time cannot be negative")
    @Max(value = 240, message = "Preparation time cannot exceed 240 minutes")
    @Column(nullable = false)
    private Integer preparationTime = 15; // in minutes

    @Min(value = 0, message = "Calories cannot be negative")
    @Column
    private Integer calories;

    @Size(max = 200, message = "Ingredients list must not exceed 200 characters")
    @Column(length = 200)
    private String ingredients;

    @Size(max = 200, message = "Allergens list must not exceed 200 characters")
    @Column(length = 200)
    private String allergens;

    // Display order for menu presentation
    @Column(nullable = false)
    private Integer displayOrder = 0;

    // Audit fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonBackReference("restaurant-menuItems")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonBackReference("category-menuItems")
    private MenuCategory category;

    // Constructors
    public MenuItem() {}

    public MenuItem(String name, String description, BigDecimal price, Restaurant restaurant) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.restaurant = restaurant;
    }

    public MenuItem(String name, String description, BigDecimal price, String imageUrl,
                   Restaurant restaurant, MenuCategory category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.restaurant = restaurant;
        this.category = category;
    }

    // Business Logic Methods
    
    /**
     * Check if menu item can be ordered
     */
    public boolean canBeOrdered() {
        return isAvailable && restaurant != null && restaurant.canAcceptOrders();
    }

    /**
     * Get total preparation time including restaurant's base time
     */
    public Integer getTotalPreparationTime() {
        if (restaurant != null && restaurant.getEstimatedDeliveryTime() != null) {
            return preparationTime + (restaurant.getEstimatedDeliveryTime() / 2);
        }
        return preparationTime;
    }

    /**
     * Check if item has dietary restrictions
     */
    public boolean hasDietaryRestrictions() {
        return isVegetarian || isVegan || isGlutenFree;
    }

    /**
     * Get dietary tags as string
     */
    public String getDietaryTags() {
        StringBuilder tags = new StringBuilder();
        
        if (isVegan) tags.append("Vegan, ");
        else if (isVegetarian) tags.append("Vegetarian, ");
        
        if (isGlutenFree) tags.append("Gluten-Free, ");
        if (isSpicy) tags.append("Spicy, ");
        
        String result = tags.toString();
        return result.isEmpty() ? result : result.substring(0, result.length() - 2);
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

    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public MenuCategory getCategory() { return category; }
    public void setCategory(MenuCategory category) { this.category = category; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(id, menuItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
