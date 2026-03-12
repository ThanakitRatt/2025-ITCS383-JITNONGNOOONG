import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { useApp } from '../../contexts/AppContext';
import { ArrowLeft, Clock, Loader2, RefreshCw } from 'lucide-react';
import { format } from 'date-fns';
import { toast } from 'sonner';
import orderService from '../../services/order.service';
import restaurantService from '../../services/restaurant.service';

export default function RestaurantOrders() {
  const navigate = useNavigate();
  const { user } = useApp();
  const ownerId = user?.id || '1';
  const [restaurantId, setRestaurantId] = useState<string | null>(null);

  const [orders, setOrders] = useState<any[]>([]);
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

  const handleStatusChange = async (orderId: string, newStatus: any) => {
    setUpdatingId(orderId);
    try {
      await orderService.updateOrderStatus(orderId, { 
        newStatus: newStatus,
        updatedBy: Number(restaurantId) 
      });
      setOrders(orders.map(o => o.id === orderId ? { ...o, status: newStatus } : o));
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

  // Get valid next statuses based on current status
  const getValidNextStatuses = (currentStatus: string): Array<{ value: string; label: string }> => {
    const statusFlow: Record<string, Array<{ value: string; label: string }>> = {
      PENDING: [
        { value: 'CONFIRMED', label: 'Confirm Order' },
        { value: 'CANCELLED', label: 'Cancel Order' },
      ],
      CONFIRMED: [
        { value: 'PREPARING', label: 'Start Preparing' },
        { value: 'CANCELLED', label: 'Cancel Order' },
      ],
      PREPARING: [
        { value: 'READY_FOR_PICKUP', label: 'Ready for Pickup' },
        { value: 'CANCELLED', label: 'Cancel Order' },
      ],
      READY_FOR_PICKUP: [
        { value: 'PICKED_UP', label: 'Mark as Picked Up' },
      ],
      PICKED_UP: [
        { value: 'DELIVERED', label: 'Mark as Delivered' },
      ],
      // Final statuses have no next status options
      DELIVERED: [],
      CANCELLED: [],
      REFUNDED: [],
    };

    return statusFlow[currentStatus] || [];
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
                      <CardTitle className="text-lg">Order #{order.id}</CardTitle>
                      <p className="text-sm text-gray-500 mt-1">
                        Customer: {order.customerName}
                      </p>
                      <div className="flex items-center gap-1 text-sm text-gray-500 mt-1">
                        <Clock className="w-4 h-4" />
                        {format(order.createdAt, 'PPp')}
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
                      {(order.items || []).map((item, idx) => (
                        <div key={idx} className="flex justify-between text-sm">
                          <span>{item.name} × {item.quantity}</span>
                          <span>฿{(item.price * item.quantity).toFixed(2)}</span>
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
                    <div className="flex items-center gap-4">
                      {getValidNextStatuses(order.status).length > 0 ? (
                        <div className="w-48">
                          <Select
                            value={order.status}
                            onValueChange={(value) => handleStatusChange(order.id, value)}
                            disabled={updatingId === order.id}
                          >
                            <SelectTrigger>
                              <SelectValue placeholder="Update status..." />
                            </SelectTrigger>
                            <SelectContent>
                              {getValidNextStatuses(order.status).map((status) => (
                                <SelectItem key={status.value} value={status.value}>
                                  {status.label}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </div>
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
