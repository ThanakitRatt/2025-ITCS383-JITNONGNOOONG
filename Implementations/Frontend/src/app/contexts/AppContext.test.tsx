import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, afterEach } from 'vitest';
import React from 'react';
import { AppProvider, useApp } from './AppContext';
import type { CartItem, Order } from './AppContext';

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <AppProvider>{children}</AppProvider>
);

const makeCart = (id: string): CartItem => ({
  id,
  name: 'Test Item',
  price: 100,
  quantity: 1,
  restaurantId: 'R1',
  restaurantName: 'Test Restaurant',
});

const makeOrder = (id: string): Order => ({
  id,
  customerId: 'CUST1',
  customerName: 'Test Customer',
  restaurantId: 'R1',
  restaurantName: 'Test Restaurant',
  items: [],
  total: 200,
  status: 'pending',
  createdAt: new Date(),
  deliveryAddress: '123 Test St',
  paymentMethod: 'credit-card',
});

// ─── localStorage rehydration ────────────────────────────────────────────────

describe('AppProvider – localStorage rehydration', () => {
  afterEach(() => localStorage.clear());

  it('initialises user as null when localStorage is empty', () => {
    const { result } = renderHook(() => useApp(), { wrapper });
    expect(result.current.user).toBeNull();
  });

  it('rehydrates user from all four keys', () => {
    localStorage.setItem('userId', 'U1');
    localStorage.setItem('userRole', 'CUSTOMER');
    localStorage.setItem('userName', 'Alice');
    localStorage.setItem('userEmail', 'alice@test.com');

    const { result } = renderHook(() => useApp(), { wrapper });
    expect(result.current.user).toEqual({
      id: 'U1',
      role: 'customer', // normalised to lowercase
      name: 'Alice',
      email: 'alice@test.com',
    });
  });

  it('does not rehydrate when any required key is missing', () => {
    localStorage.setItem('userId', 'U1');
    localStorage.setItem('userRole', 'customer');
    // name and email intentionally omitted
    const { result } = renderHook(() => useApp(), { wrapper });
    expect(result.current.user).toBeNull();
  });
});

// ─── persistUser (setUser) ────────────────────────────────────────────────────

describe('AppProvider – setUser / persistUser', () => {
  afterEach(() => localStorage.clear());

  it('writes all keys to localStorage when setUser is called with a user', () => {
    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.setUser({ id: 'U2', role: 'customer', name: 'Bob', email: 'bob@test.com' });
    });

    expect(localStorage.getItem('userId')).toBe('U2');
    expect(localStorage.getItem('userRole')).toBe('customer');
    expect(localStorage.getItem('userName')).toBe('Bob');
    expect(localStorage.getItem('userEmail')).toBe('bob@test.com');
    expect(result.current.user?.name).toBe('Bob');
  });

  it('removes all auth keys from localStorage when setUser(null) is called', () => {
    localStorage.setItem('userId', 'U3');
    localStorage.setItem('userRole', 'customer');
    localStorage.setItem('userName', 'Carol');
    localStorage.setItem('userEmail', 'carol@test.com');
    localStorage.setItem('authToken', 'tok123');
    localStorage.setItem('refreshToken', 'ref456');

    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.setUser(null);
    });

    expect(localStorage.getItem('userId')).toBeNull();
    expect(localStorage.getItem('userRole')).toBeNull();
    expect(localStorage.getItem('authToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(result.current.user).toBeNull();
  });
});

// ─── logout ──────────────────────────────────────────────────────────────────

describe('AppProvider – logout', () => {
  afterEach(() => localStorage.clear());

  it('clears user and cart on logout', () => {
    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.setUser({ id: 'U4', role: 'customer', name: 'Dave', email: 'd@t.com' });
      result.current.addToCart(makeCart('i1'));
    });

    act(() => {
      result.current.logout();
    });

    expect(result.current.user).toBeNull();
    expect(result.current.cart).toHaveLength(0);
    expect(localStorage.getItem('userId')).toBeNull();
  });
});

// ─── cart operations ─────────────────────────────────────────────────────────

describe('AppProvider – cart operations', () => {
  it('adds a new item to cart', () => {
    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.addToCart(makeCart('i1'));
    });

    expect(result.current.cart).toHaveLength(1);
    expect(result.current.cart[0].id).toBe('i1');
  });

  it('increments quantity when the same item is added again', () => {
    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.addToCart({ ...makeCart('i2'), quantity: 1 });
      result.current.addToCart({ ...makeCart('i2'), quantity: 3 });
    });

    expect(result.current.cart).toHaveLength(1);
    expect(result.current.cart[0].quantity).toBe(4);
  });

  it('removes an item from cart', () => {
    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.addToCart(makeCart('i3'));
    });

    act(() => {
      result.current.removeFromCart('i3');
    });

    expect(result.current.cart).toHaveLength(0);
  });

  it('updates item quantity in cart', () => {
    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.addToCart(makeCart('i4'));
    });

    act(() => {
      result.current.updateCartQuantity('i4', 7);
    });

    expect(result.current.cart[0].quantity).toBe(7);
  });

  it('removes item when quantity is updated to 0', () => {
    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.addToCart(makeCart('i5'));
    });

    act(() => {
      result.current.updateCartQuantity('i5', 0);
    });

    expect(result.current.cart).toHaveLength(0);
  });

  it('clears all items from cart', () => {
    const { result } = renderHook(() => useApp(), { wrapper });

    act(() => {
      result.current.addToCart(makeCart('i6'));
      result.current.addToCart(makeCart('i7'));
    });

    act(() => {
      result.current.clearCart();
    });

    expect(result.current.cart).toHaveLength(0);
  });
});

// ─── order operations ────────────────────────────────────────────────────────

describe('AppProvider – order operations', () => {
  it('adds a new order to the front of the list', () => {
    const { result } = renderHook(() => useApp(), { wrapper });
    const initialCount = result.current.orders.length;

    act(() => {
      result.current.addOrder(makeOrder('NEW_ORD'));
    });

    expect(result.current.orders).toHaveLength(initialCount + 1);
    expect(result.current.orders[0].id).toBe('NEW_ORD');
  });

  it('updates an existing order by id', () => {
    const { result } = renderHook(() => useApp(), { wrapper });
    const firstOrderId = result.current.orders[0].id;

    act(() => {
      result.current.updateOrder(firstOrderId, { status: 'completed' });
    });

    const updated = result.current.orders.find(o => o.id === firstOrderId);
    expect(updated?.status).toBe('completed');
  });
});
