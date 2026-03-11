package com.itcs383.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.itcs383.common.dto.MenuItemDTO;
import com.itcs383.common.exception.ResourceNotFoundException;
import com.itcs383.restaurant.dto.UpdateMenuItemRequest;
import com.itcs383.restaurant.entity.MenuCategory;
import com.itcs383.restaurant.entity.MenuItem;
import com.itcs383.restaurant.entity.Restaurant;
import com.itcs383.restaurant.repository.MenuCategoryRepository;
import com.itcs383.restaurant.repository.MenuItemRepository;
import com.itcs383.restaurant.repository.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private MenuCategoryRepository menuCategoryRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant mockRestaurant;
    private MenuItem baseMenuItem;

    @BeforeEach
    void setUp() {
        mockRestaurant = mock(Restaurant.class);
        when(mockRestaurant.getId()).thenReturn(1L);
        when(mockRestaurant.getName()).thenReturn("Test Restaurant");

        baseMenuItem = new MenuItem("Pad Thai", "Stir-fried rice noodles",
                new BigDecimal("120.00"), mockRestaurant);
    }

    // ========== updateMenuItem tests ==========

    @Test
    void updateMenuItem_WithAllFields_ShouldApplyAllUpdates() {
        UpdateMenuItemRequest request = new UpdateMenuItemRequest();
        request.setName("Updated Pad Thai");
        request.setDescription("Improved recipe");
        request.setPrice(new BigDecimal("135.00"));
        request.setImageUrl("https://example.com/pad-thai.jpg");
        request.setIsAvailable(false);
        request.setIsFeatured(true);
        request.setIsVegetarian(true);
        request.setIsVegan(false);
        request.setIsGlutenFree(true);
        request.setIsSpicy(true);
        request.setPreparationTime(25);
        request.setCalories(520);
        request.setIngredients("Rice noodles, tofu, bean sprouts");
        request.setAllergens("Peanuts, soy");
        request.setDisplayOrder(2);

        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(baseMenuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(baseMenuItem);

        MenuItemDTO result = restaurantService.updateMenuItem(1L, 10L, request);

        assertThat(result).isNotNull();
        assertThat(baseMenuItem.getName()).isEqualTo("Updated Pad Thai");
        assertThat(baseMenuItem.getDescription()).isEqualTo("Improved recipe");
        assertThat(baseMenuItem.getPrice()).isEqualByComparingTo("135.00");
        assertThat(baseMenuItem.getImageUrl()).isEqualTo("https://example.com/pad-thai.jpg");
        assertThat(baseMenuItem.getIsAvailable()).isFalse();
        assertThat(baseMenuItem.getIsFeatured()).isTrue();
        assertThat(baseMenuItem.getIsVegetarian()).isTrue();
        assertThat(baseMenuItem.getIsGlutenFree()).isTrue();
        assertThat(baseMenuItem.getIsSpicy()).isTrue();
        assertThat(baseMenuItem.getPreparationTime()).isEqualTo(25);
        assertThat(baseMenuItem.getCalories()).isEqualTo(520);
        assertThat(baseMenuItem.getDisplayOrder()).isEqualTo(2);
        verify(menuItemRepository).save(baseMenuItem);
    }

    @Test
    void updateMenuItem_WithNullFields_ShouldLeaveExistingValuesUnchanged() {
        UpdateMenuItemRequest request = new UpdateMenuItemRequest(); // all fields null

        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(baseMenuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(baseMenuItem);

        MenuItemDTO result = restaurantService.updateMenuItem(1L, 10L, request);

        assertThat(result).isNotNull();
        assertThat(baseMenuItem.getName()).isEqualTo("Pad Thai");
        assertThat(baseMenuItem.getPrice()).isEqualByComparingTo("120.00");
    }

    @Test
    void updateMenuItem_WithCategoryId_ShouldAssignCategory() {
        MenuCategory category = new MenuCategory("Noodles", "Noodle dishes", mockRestaurant);

        UpdateMenuItemRequest request = new UpdateMenuItemRequest();
        request.setCategoryId(2L);

        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(baseMenuItem));
        when(menuCategoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(baseMenuItem);

        restaurantService.updateMenuItem(1L, 10L, request);

        assertThat(baseMenuItem.getCategory()).isEqualTo(category);
        verify(menuCategoryRepository).findById(2L);
    }

    @Test
    void updateMenuItem_WhenItemNotFound_ShouldThrowResourceNotFoundException() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                restaurantService.updateMenuItem(1L, 99L, new UpdateMenuItemRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateMenuItem_WhenItemBelongsToDifferentRestaurant_ShouldThrowIllegalArgumentException() {
        Restaurant otherRestaurant = mock(Restaurant.class);
        when(otherRestaurant.getId()).thenReturn(99L);
        MenuItem otherItem = new MenuItem("Sushi", "Fresh sushi", new BigDecimal("200.00"), otherRestaurant);

        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(otherItem));

        assertThatThrownBy(() ->
                restaurantService.updateMenuItem(1L, 10L, new UpdateMenuItemRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to this restaurant");
    }

    @Test
    void updateMenuItem_WithInvalidCategoryId_ShouldThrowResourceNotFoundException() {
        UpdateMenuItemRequest request = new UpdateMenuItemRequest();
        request.setCategoryId(999L);

        when(menuItemRepository.findById(10L)).thenReturn(Optional.of(baseMenuItem));
        when(menuCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                restaurantService.updateMenuItem(1L, 10L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
