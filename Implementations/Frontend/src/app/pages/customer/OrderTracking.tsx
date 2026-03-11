import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Progress } from '../../components/ui/progress';
import { useApp } from '../../contexts/AppContext';
import { ArrowLeft, Package, ChefHat, Bike, CheckCircle, Loader2 } from 'lucide-react';
import { format } from 'date-fns';
import orderService, { Order, OrderStatus } from '../../services/order.service';

const statusConfig: Record<OrderStatus, { label: string; icon: typeof Package; color: string; progress: number }> = {
  [OrderStatus.PENDING]: { label: 'Order Placed', icon: Package, color: 'bg-blue-500', progress: 20 },
  [OrderStatus.CONFIRMED]: { label: 'Confirmed', icon: Package, color: 'bg-blue-600', progress: 35 },
  [OrderStatus.PREPARING]: { label: 'Preparing', icon: ChefHat, color: 'bg-orange-500', progress: 50 },
  [OrderStatus.READY_FOR_PICKUP]: { label: 'Ready for Pickup', icon: Package, color: 'bg-purple-500', progress: 65 },
  [OrderStatus.PICKED_UP]: { label: 'Picked Up', icon: Bike, color: 'bg-indigo-500', progress: 75 },
  [OrderStatus.DELIVERED]: { label: 'Delivered', icon: CheckCircle, color: 'bg-green-600', progress: 100 },
  [OrderStatus.CANCELLED]: { label: 'Cancelled', icon: Package, color: 'bg-red-500', progress: 0 },
  [OrderStatus.REFUNDED]: { label: 'Refunded', icon: Package, color: 'bg-gray-500', progress: 0 },
};

export default function OrderTracking() {
  const navigate = useNavigate();
  const { user } = useApp();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user?.id) {
      setLoading(false);
      return;
    }
    orderService
      .getCustomerOrders(user.id.toString())
      .then(response => setOrders(response.content ?? []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, [user?.id]);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <Button variant="ghost" onClick={() => navigate('/customer/restaurants')}>
              <ArrowLeft className="w-4 h-4 mr-2" />
              Back to Restaurants
            </Button>
            <h1 className="text-xl">My Orders</h1>
            <div className="w-24" /> {/* Spacer for alignment */}
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        {loading && (
          <div className="flex justify-center py-16">
            <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
          </div>
        )}
        {!loading && orders.length === 0 && (
          <Card>
            <CardContent className="text-center py-12">
              <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <p className="text-gray-500 mb-4">No orders yet</p>
              <Button onClick={() => navigate('/customer/restaurants')}>
                Start Ordering
              </Button>
            </CardContent>
          </Card>
        )}
        {!loading && orders.length > 0 && (
          <div className="space-y-6">
            {orders.map(order => {
              const config = statusConfig[order.status] ?? statusConfig[OrderStatus.PENDING];
              const Icon = config.icon;
              
              return (
                <Card key={order.id}>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="text-lg">Order #{order.orderNumber}</CardTitle>
                        <p className="text-sm text-gray-500">
                          {format(new Date(order.createdAt), 'PPp')}
                        </p>
                      </div>
                      <Badge className={config.color}>{config.label}</Badge>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {/* Order Progress */}
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <Icon className="w-4 h-4" />
                        <span className="text-sm font-semibold">{config.label}</span>
                      </div>
                      <Progress value={config.progress} />
                    </div>

                    {/* Order Items */}
                    <div className="space-y-2">
                      <p className="text-sm font-semibold">Items:</p>
                      {(order.orderItems ?? []).map(item => (
                        <div key={item.id} className="flex justify-between text-sm">
                          <span>{item.menuItemName} × {item.quantity}</span>
                          <span>฿{item.totalPrice.toFixed(2)}</span>
                        </div>
                      ))}
                    </div>

                    {/* Delivery Info */}
                    <div className="pt-4 border-t">
                      <p className="text-sm text-gray-600">
                        {order.deliveryAddress}
                      </p>
                    </div>

                    {/* Total */}
                    <div className="pt-2 border-t flex items-center justify-between">
                      <div>
                        <p className="text-sm text-gray-500">Total Amount</p>
                        <p className="text-lg font-semibold">฿{(order.totalAmount ?? 0).toFixed(2)}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
