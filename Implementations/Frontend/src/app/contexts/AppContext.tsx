import React, { createContext, useContext, useState, useMemo, ReactNode } from 'react';

export type UserRole = 'customer' | 'restaurant' | 'rider' | 'admin' | null;

export interface CartItem {
  id: string;
  name: string;
  price: number;
  quantity: number;
  restaurantId: string;
  restaurantName: string;
}

export interface Order {
  id: string;
  customerId: string;
  customerName: string;
  restaurantId: string;
  restaurantName: string;
  riderId?: string;
  riderName?: string;
  items: CartItem[];
  total: number;
  status: 'pending' | 'preparing' | 'ready' | 'delivering' | 'completed' | 'cancelled';
  createdAt: Date;
  deliveryAddress: string;
  paymentMethod: 'credit-card' | 'qr-code';
  rating?: number;
  riderRating?: {
    politeness: number;
    speed: number;
    comment?: string;
  };
}

interface User {
  id: string;
  role: UserRole;
  name: string;
  email: string;
  restaurantId?: string;
}

interface AppContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  cart: CartItem[];
  addToCart: (item: CartItem) => void;
  removeFromCart: (itemId: string) => void;
  updateCartQuantity: (itemId: string, quantity: number) => void;
  clearCart: () => void;
  orders: Order[];
  addOrder: (order: Order) => void;
  updateOrder: (orderId: string, updates: Partial<Order>) => void;
  logout: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
};

export const AppProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const id = localStorage.getItem('userId');
    const role = localStorage.getItem('userRole');
    const name = localStorage.getItem('userName');
    const email = localStorage.getItem('userEmail');
    if (id && role && name && email) {
      return { id, role: role.toLowerCase() as UserRole, name, email };
    }
    return null;
  });

  const persistUser = (u: User | null) => {
    if (u) {
      localStorage.setItem('userId', u.id);
      localStorage.setItem('userRole', u.role ?? '');
      localStorage.setItem('userName', u.name);
      localStorage.setItem('userEmail', u.email);
    } else {
      localStorage.removeItem('userId');
      localStorage.removeItem('userRole');
      localStorage.removeItem('userName');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
    }
    setUser(u);
  };
  const [cart, setCart] = useState<CartItem[]>([]);
  const [orders, setOrders] = useState<Order[]>(() => {
    const stored = localStorage.getItem('app_orders');
    if (stored) {
      try {
        const parsed: Order[] = JSON.parse(stored);
        return parsed.map(o => ({ ...o, createdAt: new Date(o.createdAt) }));
      } catch { /* fall through to initial data */ }
    }
    return [
    // Mock existing orders
    {
      id: 'ORD001',
      customerId: 'CUST001',
      customerName: 'John Doe',
      restaurantId: 'REST001',
      restaurantName: 'Bangkok Street Food',
      riderId: '200',
      riderName: 'Mike Chen',
      items: [
        { id: 'ITEM001', name: 'Pad Thai', price: 120, quantity: 2, restaurantId: 'REST001', restaurantName: 'Bangkok Street Food' }
      ],
      total: 264, // 240 + 10% commission
      status: 'delivering',
      createdAt: new Date(Date.now() - 1000 * 60 * 15),
      deliveryAddress: '123 Sukhumvit Road, Bangkok',
      paymentMethod: 'credit-card'
    },
    {
      id: 'ORD002',
      customerId: 'CUST002',
      customerName: 'Sarah Wilson',
      restaurantId: 'REST002',
      restaurantName: 'Sushi Master',
      items: [
        { id: 'ITEM002', name: 'Salmon Sashimi', price: 350, quantity: 1, restaurantId: 'REST002', restaurantName: 'Sushi Master' }
      ],
      total: 385,
      status: 'preparing',
      createdAt: new Date(Date.now() - 1000 * 60 * 5),
      deliveryAddress: '456 Silom Road, Bangkok',
      paymentMethod: 'qr-code'
    }
  ];
  });

  const addToCart = (item: CartItem) => {
    setCart(prev => {
      const existing = prev.find(i => i.id === item.id);
      if (existing) {
        return prev.map(i => 
          i.id === item.id 
            ? { ...i, quantity: i.quantity + item.quantity }
            : i
        );
      }
      return [...prev, item];
    });
  };

  const removeFromCart = (itemId: string) => {
    setCart(prev => prev.filter(i => i.id !== itemId));
  };

  const updateCartQuantity = (itemId: string, quantity: number) => {
    if (quantity <= 0) {
      removeFromCart(itemId);
    } else {
      setCart(prev => prev.map(i => 
        i.id === itemId ? { ...i, quantity } : i
      ));
    }
  };

  const clearCart = () => {
    setCart([]);
  };

  const addOrder = (order: Order) => {
    setOrders(prev => {
      const next = [order, ...prev];
      localStorage.setItem('app_orders', JSON.stringify(next));
      return next;
    });
  };

  const updateOrder = (orderId: string, updates: Partial<Order>) => {
    setOrders(prev => {
      const next = prev.map(order =>
        order.id === orderId ? { ...order, ...updates } : order
      );
      localStorage.setItem('app_orders', JSON.stringify(next));
      return next;
    });
  };

  const logout = () => {
    persistUser(null);
    setCart([]);
  };

  const contextValue = useMemo(() => ({
    user,
    setUser: persistUser,
    cart,
    addToCart,
    removeFromCart,
    updateCartQuantity,
    clearCart,
    orders,
    addOrder,
    updateOrder,
    logout
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }), [user, cart, orders]);

  return (
    <AppContext.Provider value={contextValue}>
      {children}
    </AppContext.Provider>
  );
};
