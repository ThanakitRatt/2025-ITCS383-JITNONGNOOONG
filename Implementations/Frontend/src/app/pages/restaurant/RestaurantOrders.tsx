import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { useApp } from '../../contexts/AppContext';
import { ArrowLeft, Clock, Loader2, RefreshCw } from 'lucide-react';
import { format } from 'date-fns';
import { toast } from 'sonner';
import orderService from '../../services/order.service';
import restaurantService from '../../services/restaurant.service';

type RestaurantOrder = {
  id: number | string;
  orderNumber?: string;
  customerId?: number | string;
  customerName?: string;
  status: string;
  totalAmount?: number;
  total?: number;
  deliveryAddress?: string;
  createdAt?: string;
  orderItems?: Array<{
    id?: string | number;
    menuItemName?: string;
    name?: string;
    quantity?: number;
    totalPrice?: number;
    unitPrice?: number;
    price?: number;
  }>;
  items?: Array<{
    id?: string | number;
    menuItemName?: string;
    name?: string;
    quantity?: number;
    totalPrice?: number;
    unitPrice?: number;
    price?: number;
  }>;
};

type OrderAction = {
  value: string;
  label: string;
  variant?: 'default' | 'outline' | 'destructive';
};

const statusFlow: Record<string, OrderAction[]> = {
  PENDING: [
    { value: 'CONFIRMED', label: 'Confirm Order' },
    { value: 'CANCELLED', label: 'Cancel Order', variant: 'destructive' },
  ],
  CONFIRMED: [
    { value: 'PREPARING', label: 'Start Preparing' },
    { value: 'CANCELLED', label: 'Cancel Order', variant: 'destructive' },
  ],
  PREPARING: [
    { value: 'READY_FOR_PICKUP', label: 'Ready for Pickup' },
    { value: 'CANCELLED', label: 'Cancel Order', variant: 'destructive' },
  ],
  READY_FOR_PICKUP: [
    { value: 'PICKED_UP', label: 'Mark as Picked Up', variant: 'outline' },
  ],
  PICKED_UP: [
    { value: 'DELIVERED', label: 'Mark as Delivered', variant: 'outline' },
  ],
  DELIVERED: [],
  CANCELLED: [],
  REFUNDED: [],
};

export default function RestaurantOrders() {
  const navigate = useNavigate();
  const { user } = useApp();
  const ownerId = user?.id || '1';
  const [restaurantId, setRestaurantId] = useState<string | null>(null);

  const [orders, setOrders] = useState<RestaurantOrder[]>([]);
  const [loadingData, setLoadingData] = useState(true);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  useEffect(() => {
    const initRestaurant = async () => {
      try {
        const restaurants = await restaurantService.getOwnerRestaurants(ownerId);
        if (restaurants && restaurants.length > 0) {
          setRestaurantId(restaurants[0].id.toString());
        }
      } catch (error) {
        console.error('Failed to load restaurant:', error);
        toast.error('Failed to load restaurant');
      }
    };
    initRestaurant();
  }, [ownerId]);

  useEffect(() => {
    if (restaurantId) {
      loadOrders();
    }
  }, [restaurantId]);

  const loadOrders = async () => {
    if (!restaurantId) return;
    try {
      setLoadingData(true);
      const result = await orderService.getRestaurantOrders(restaurantId);
      setOrders(result.content || []);
    } catch (error) {
      console.error('Failed to load orders:', error);
      toast.error('Failed to load orders');
      setOrders([]);
    } finally {
      setLoadingData(false);
    }
  };

  const handleStatusChange = async (orderId: number | string, newStatus: string) => {
    const orderIdKey = String(orderId);
    setUpdatingId(orderIdKey);
    try {
      await orderService.updateOrderStatus(orderId, {
        newStatus: newStatus,
        updatedBy: Number(restaurantId)
      });
      setOrders((currentOrders) =>
        currentOrders.map((order) =>
          String(order.id) === orderIdKey ? { ...order, status: newStatus } : order
        )
      );
      toast.success('Order status updated');
    } catch (error) {
      console.error('Failed to update order status:', error);
      toast.error('Failed to update order status');
    } finally {
      setUpdatingId(null);
    }
  };

  const statusColors: Record<string, string> = {
    PENDING: 'bg-blue-500',
    CONFIRMED: 'bg-blue-600',
    PREPARING: 'bg-orange-500',
    READY_FOR_PICKUP: 'bg-purple-500',
    PICKED_UP: 'bg-indigo-500',
    DELIVERED: 'bg-green-600',
    CANCELLED: 'bg-red-500',
    REFUNDED: 'bg-gray-500',
  };

  const getValidNextStatuses = (currentStatus: string): OrderAction[] => {
    return statusFlow[currentStatus] || [];
  };

  const getOrderItems = (order: RestaurantOrder) => {
    return order.orderItems || order.items || [];
  };

  const getOrderCreatedAt = (order: RestaurantOrder) => {
    if (!order.createdAt) {
      return 'Date unavailable';
    }

    const parsedDate = new Date(order.createdAt);
    return Number.isNaN(parsedDate.getTime()) ? 'Date unavailable' : format(parsedDate, 'PPp');
  };

  const getCustomerLabel = (order: RestaurantOrder) => {
    if (order.customerName) {
      return order.customerName;
    }

    if (order.customerId !== undefined && order.customerId !== null) {
      return `Customer #${order.customerId}`;
    }

    return 'Customer unavailable';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl">Orders Management</h1>
            <div className="flex gap-2">
              <Button variant="outline" onClick={loadOrders} disabled={loadingData}>
                <RefreshCw className={`w-4 h-4 mr-2 ${loadingData ? 'animate-spin' : ''}`} />
                Refresh
              </Button>
              <Button variant="ghost" onClick={() => navigate('/restaurant/dashboard')}>
                <ArrowLeft className="w-4 h-4 mr-2" />
                Dashboard
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {loadingData ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
          </div>
        ) : orders.length === 0 ? (
          <Card>
            <CardContent className="text-center py-12">
              <p className="text-gray-500">No orders yet</p>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-4">
            {orders.map(order => (
              <Card key={order.id}>
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div>
                      <CardTitle className="text-lg">Order #{order.orderNumber ?? order.id}</CardTitle>
                      <p className="text-sm text-gray-500 mt-1">
                        Customer: {getCustomerLabel(order)}
                      </p>
                      <div className="flex items-center gap-1 text-sm text-gray-500 mt-1">
                        <Clock className="w-4 h-4" />
                        {getOrderCreatedAt(order)}
                      </div>
                    </div>
                    <Badge className={statusColors[order.status]}>
                      {order.status}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  {/* Order Items */}
                  <div>
                    <p className="text-sm font-semibold mb-2">Items:</p>
                    <div className="space-y-1">
                      {getOrderItems(order).map((item, idx) => (
                        <div key={idx} className="flex justify-between text-sm">
                          <span>{item.menuItemName ?? item.name ?? 'Item'} × {item.quantity ?? 0}</span>
                          <span>
                            ฿
                            {(
                              item.totalPrice ??
                              (item.unitPrice ?? item.price ?? 0) * (item.quantity ?? 0)
                            ).toFixed(2)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Delivery Address */}
                  <div className="pt-2 border-t">
                    <p className="text-sm font-semibold mb-1">Delivery Address:</p>
                    <p className="text-sm text-gray-600">{order.deliveryAddress}</p>
                  </div>

                  {/* Actions */}
                  <div className="pt-4 border-t flex items-center justify-between">
                    <div>
                      <p className="text-sm text-gray-500">Total Amount</p>
                      <p className="text-lg font-semibold">฿{(order.totalAmount ?? order.total ?? 0).toFixed(2)}</p>
                    </div>
                    <div className="flex flex-wrap items-center justify-end gap-2">
                      {getValidNextStatuses(order.status).length > 0 ? (
                        getValidNextStatuses(order.status).map((status) => (
                          <Button
                            key={status.value}
                            variant={status.variant ?? 'default'}
                            onClick={() => handleStatusChange(order.id, status.value)}
                            disabled={updatingId === String(order.id)}
                          >
                            {updatingId === String(order.id) ? (
                              <Loader2 className="w-4 h-4 animate-spin" />
                            ) : (
                              status.label
                            )}
                          </Button>
                        ))
                      ) : (
                        <Badge className={`${statusColors[order.status] || 'bg-gray-500'} text-white px-3 py-1`}>
                          {order.status}
                        </Badge>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
