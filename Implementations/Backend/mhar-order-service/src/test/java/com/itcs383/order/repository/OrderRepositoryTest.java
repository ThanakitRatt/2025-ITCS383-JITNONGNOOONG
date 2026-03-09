package com.itcs383.order.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.itcs383.common.enums.OrderStatus;
import com.itcs383.order.entity.Order;
import com.itcs383.order.entity.OrderItem;

/**
 * Repository Tests for Order Entity
 * Tests JPA queries, relationships, and database constraints
 */
@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder1;
    private Order testOrder2;
    private Order testOrder3;

    @BeforeEach
    void setUp() {
        // Clean database
        orderRepository.deleteAll();
        entityManager.flush();

        // Create test orders
        testOrder1 = createTestOrder(1L, 1L, OrderStatus.PENDING, "MR001");
        testOrder2 = createTestOrder(1L, 2L, OrderStatus.CONFIRMED, "MR002");
        testOrder3 = createTestOrder(2L, 1L, OrderStatus.DELIVERED, "MR003");

        // Save orders
        testOrder1 = entityManager.persistAndFlush(testOrder1);
        testOrder2 = entityManager.persistAndFlush(testOrder2);
        testOrder3 = entityManager.persistAndFlush(testOrder3);
    }

    @Test
    void findByOrderNumber_WithValidNumber_ShouldReturnOrder() {
        // When
        Optional<Order> found = orderRepository.findByOrderNumber("MR001");

        // Then
        assertTrue(found.isPresent());
        assertEquals(testOrder1.getId(), found.get().getId());
        assertEquals("MR001", found.get().getOrderNumber());
    }

    @Test
    void findByOrderNumber_WithInvalidNumber_ShouldReturnEmpty() {
        // When
        Optional<Order> found = orderRepository.findByOrderNumber("INVALID");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByCustomerIdOrderByCreatedAtDesc_ShouldReturnCustomerOrdersSortedByDate() {
        // Given
        Long customerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);

        // Then
        assertEquals(2, orders.getTotalElements());
        assertEquals(2, orders.getContent().size());
        
        // Verify sorting (most recent first)
        List<Order> orderList = orders.getContent();
        assertTrue(orderList.get(0).getCreatedAt().isAfter(orderList.get(1).getCreatedAt()) ||
                  orderList.get(0).getCreatedAt().isEqual(orderList.get(1).getCreatedAt()));
    }

    @Test
    void findByCustomerIdAndStatusOrderByCreatedAtDesc_ShouldReturnFilteredOrders() {
        // Given
        Long customerId = 1L;
        OrderStatus status = OrderStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> orders = orderRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
            customerId, status, pageable);

        // Then
        assertEquals(1, orders.getTotalElements());
        assertEquals(testOrder1.getId(), orders.getContent().get(0).getId());
        assertEquals(OrderStatus.PENDING, orders.getContent().get(0).getStatus());
    }

    @Test
    void findByRestaurantIdOrderByCreatedAtDesc_ShouldReturnRestaurantOrders() {
        // Given
        Long restaurantId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);

        // Then
        assertEquals(2, orders.getTotalElements());
        assertEquals(2, orders.getContent().size());
        
        // Verify all orders belong to restaurant 1
        orders.getContent().forEach(order -> {
            assertEquals(restaurantId, order.getRestaurantId());
        });
    }

    @Test
    void findByRestaurantIdAndStatusOrderByCreatedAtAsc_ShouldReturnFilteredRestaurantOrders() {
        // Given
        Long restaurantId = 1L;
        OrderStatus status = OrderStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Order> orders = orderRepository.findByRestaurantIdAndStatusOrderByCreatedAtAsc(
            restaurantId, status, pageable);

        // Then
        assertEquals(1, orders.getTotalElements());
        assertEquals(testOrder1.getId(), orders.getContent().get(0).getId());
        assertEquals(OrderStatus.PENDING, orders.getContent().get(0).getStatus());
        assertEquals(restaurantId, orders.getContent().get(0).getRestaurantId());
    }

    @Test
    void findByCustomerIdAndCreatedAtBetween_ShouldReturnOrdersInDateRange() {
        // Given
        Long customerId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<Order> orders = orderRepository.findByCustomerIdAndCreatedAtBetween(
            customerId, startDate, endDate);

        // Then
        assertEquals(2, orders.size());
        orders.forEach(order -> {
            assertEquals(customerId, order.getCustomerId());
            assertTrue(order.getCreatedAt().isAfter(startDate));
            assertTrue(order.getCreatedAt().isBefore(endDate));
        });
    }

    @Test
    void countByRestaurantIdAndStatus_ShouldReturnCorrectCount() {
        // When
        long count = orderRepository.countByRestaurantIdAndStatus(1L, OrderStatus.PENDING);

        // Then
        assertEquals(1, count);
    }

    @Test
    void findByStatusAndCreatedAtBefore_ShouldReturnOldOrders() {
        // Given - Create an old order
        Order oldOrder = createTestOrder(3L, 3L, OrderStatus.PENDING, "MR004");
        oldOrder.setCreatedAt(LocalDateTime.now().minusHours(25));
        entityManager.persistAndFlush(oldOrder);

        // When
        List<Order> oldOrders = orderRepository.findByStatusAndCreatedAtBefore(
            OrderStatus.PENDING, LocalDateTime.now().minusHours(24));

        // Then
        assertEquals(1, oldOrders.size());
        assertEquals("MR004", oldOrders.get(0).getOrderNumber());
    }

    @Test
    void orderWithItems_ShouldPersistAndLoadCorrectly() {
        // Given
        Order newOrder = createTestOrder(4L, 4L, OrderStatus.PENDING, "MR005");
        
        OrderItem item1 = new OrderItem();
        item1.setMenuItemId(1L);
        item1.setMenuItemName("Pad Thai");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("120.00"));
        item1.setOrder(newOrder);
        
        OrderItem item2 = new OrderItem();
        item2.setMenuItemId(2L);
        item2.setMenuItemName("Green Curry");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("150.00"));
        item2.setOrder(newOrder);
        
        newOrder.getOrderItems().add(item1);
        newOrder.getOrderItems().add(item2);

        // When
        Order savedOrder = entityManager.persistAndFlush(newOrder);
        entityManager.clear(); // Clear persistence context
        Order loadedOrder = orderRepository.findById(savedOrder.getId()).orElse(null);

        // Then
        assertNotNull(loadedOrder);
        assertEquals(2, loadedOrder.getOrderItems().size());
        
        // Verify items are loaded correctly
        assertEquals("Pad Thai", loadedOrder.getOrderItems().get(0).getMenuItemName());
        assertEquals("Green Curry", loadedOrder.getOrderItems().get(1).getMenuItemName());
        assertEquals(new BigDecimal("120.00"), loadedOrder.getOrderItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("150.00"), loadedOrder.getOrderItems().get(1).getUnitPrice());
    }

    @Test
    void deleteOrder_ShouldCascadeDeleteItems() {
        // Given
        Order orderWithItems = createTestOrder(5L, 5L, OrderStatus.PENDING, "MR006");
        OrderItem item = new OrderItem();
        item.setMenuItemId(1L);
        item.setMenuItemName("Test Item");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setOrder(orderWithItems);
        orderWithItems.getOrderItems().add(item);
        
        Order savedOrder = entityManager.persistAndFlush(orderWithItems);
        Long orderId = savedOrder.getId();

        // When
        orderRepository.deleteById(orderId);
        entityManager.flush();

        // Then
        Optional<Order> deletedOrder = orderRepository.findById(orderId);
        assertFalse(deletedOrder.isPresent());
        
        // Verify items were also deleted (cascade)
        List<OrderItem> orphanedItems = entityManager.getEntityManager()
            .createQuery("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId", OrderItem.class)
            .setParameter("orderId", orderId)
            .getResultList();
        assertTrue(orphanedItems.isEmpty());
    }

    @Test
    void paginationAndSorting_ShouldWorkCorrectly() {
        // Create additional orders for pagination test
        for (int i = 4; i <= 15; i++) {
            Order order = createTestOrder(1L, 1L, OrderStatus.PENDING, "MR" + String.format("%03d", i));
            order.setCreatedAt(LocalDateTime.now().minusMinutes(i));
            entityManager.persist(order);
        }
        entityManager.flush();

        // Test first page
        Pageable firstPage = PageRequest.of(0, 5);
        Page<Order> firstPageResult = orderRepository.findByCustomerIdOrderByCreatedAtDesc(1L, firstPage);
        
        assertEquals(5, firstPageResult.getContent().size());
        assertEquals(14, firstPageResult.getTotalElements()); // 2 existing + 12 new
        assertEquals(3, firstPageResult.getTotalPages());
        assertTrue(firstPageResult.hasNext());
        
        // Test second page
        Pageable secondPage = PageRequest.of(1, 5);
        Page<Order> secondPageResult = orderRepository.findByCustomerIdOrderByCreatedAtDesc(1L, secondPage);
        
        assertEquals(5, secondPageResult.getContent().size());
        assertTrue(secondPageResult.hasNext());
        
        // Test last page
        Pageable lastPage = PageRequest.of(2, 5);
        Page<Order> lastPageResult = orderRepository.findByCustomerIdOrderByCreatedAtDesc(1L, lastPage);
        
        assertEquals(4, lastPageResult.getContent().size()); // Remaining items
        assertFalse(lastPageResult.hasNext());
    }

    // Helper method to create test order
    private Order createTestOrder(Long customerId, Long restaurantId, OrderStatus status, String orderNumber) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerId(customerId);
        order.setRestaurantId(restaurantId);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("250.00"));
        order.setDeliveryAddress("Test Address");
        order.setDeliveryFee(new BigDecimal("35.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }
}
