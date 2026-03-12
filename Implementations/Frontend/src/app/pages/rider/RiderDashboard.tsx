import { useNavigate } from 'react-router';
import { useEffect, useState } from 'react';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { useApp } from '../../contexts/AppContext';
import { ArrowLeft, MapPin, DollarSign, Package, Clock } from 'lucide-react';
import { toast } from 'sonner';
import { orderService, Order, OrderStatus } from '../../services/order.service';

export default function RiderDashboard() {
  const navigate = useNavigate();
  const { user, logout } = useApp();
  const [availableOrders, setAvailableOrders] = useState<Order[]>([]);
  const [myDeliveries, setMyDeliveries] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  const riderId = user?.id ?? 0;
  const activeDelivery = myDeliveries.find(o => o.status === OrderStatus.PICKED_UP);
  
  // Fetch available orders for riders
  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setLoading(true);
        
        // Fetch orders that are ready for pickup (not assigned to any rider)
        const availableOrdersResponse = await orderService.getAvailableOrdersForRiders(0, 50);
        setAvailableOrders(availableOrdersResponse.content);
        
        // Fetch this rider's deliveries (active + completed)
        if (riderId) {
          const riderOrdersResponse = await orderService.getRiderOrders(riderId, 0, 50);
          setMyDeliveries(riderOrdersResponse.content);
        }
        
      } catch (error) {
        console.error('Failed to fetch orders:', error);
        toast.error('Failed to load available orders');
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
    
    // Refresh every 30 seconds
    const interval = setInterval(fetchOrders, 30000);
    return () => clearInterval(interval);
  }, [riderId]);

  // Calculate stats
  const todayEarnings = myDeliveries
    .filter(o => 
      o.status === OrderStatus.DELIVERED && 
      new Date(o.createdAt).toDateString() === new Date().toDateString()
    )
    .reduce((sum, o) => sum + (o.deliveryFee ?? 0), 0);

  const completedToday = myDeliveries.filter(o => 
    o.status === OrderStatus.DELIVERED && 
    new Date(o.createdAt).toDateString() === new Date().toDateString()
  ).length;

  const handleExit = () => {
    logout();
    navigate('/login');
  };

  const handleAcceptOrder = async (orderId: number) => {
    try {
      // Update order status to PICKED_UP when rider accepts
      await orderService.updateOrderStatus(orderId, {
        newStatus: OrderStatus.PICKED_UP,
        updatedBy: Number(riderId),
        notes: 'Order picked up by rider'
      });
      
      toast.success('Order accepted!');
      
      // Refresh available orders
      const availableOrdersResponse = await orderService.getAvailableOrdersForRiders(0, 50);
      setAvailableOrders(availableOrdersResponse.content);
      
      navigate(`/rider/delivery/${orderId}`);
    } catch (error) {
      console.error('Failed to accept order:', error);
      toast.error('Failed to accept order. Please try again.');
    }
  };

  let availableOrdersContent;
  if (loading) {
    availableOrdersContent = (
      <div className="text-center py-8">
        <Clock className="w-16 h-16 text-gray-300 mx-auto mb-4 animate-spin" />
        <p className="text-gray-500">Loading available orders...</p>
      </div>
    );
  } else if (availableOrders.length === 0) {
    availableOrdersContent = (
      <div className="text-center py-8">
        <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
        <p className="text-gray-500">No available orders at the moment</p>
        <p className="text-sm text-gray-400 mt-2">New orders will appear here</p>
      </div>
    );
  } else {
    availableOrdersContent = (
      <div className="space-y-4">
        {availableOrders.map(order => (
          <Card key={order.id} className="border-2">
            <CardContent className="pt-6">
              <div className="space-y-3">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="font-semibold">Restaurant ID: {order.restaurantId}</p>
                    <p className="text-sm text-gray-500">Order #{order.orderNumber}</p>
                    <p className="text-sm font-semibold text-green-600">฿{order.totalAmount.toFixed(2)}</p>
                  </div>
                  <Badge className="bg-green-500">
                    ฿50 delivery fee
                  </Badge>
                </div>
                <div className="flex items-start gap-2">
                  <MapPin className="w-4 h-4 mt-1 text-gray-500 flex-shrink-0" />
                  <div className="text-sm">
                    <p className="font-semibold">Customer ID: {order.customerId}</p>
                    <p className="text-gray-600">{order.deliveryAddress}</p>
                  </div>
                </div>
                <Button
                  className="w-full"
                  onClick={() => handleAcceptOrder(order.id)}
                >
                  Accept Order
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl">🚴 Rider Portal</h1>
              <p className="text-sm text-gray-500">{user?.name ?? 'Rider'}</p>
            </div>
            <Button variant="ghost" onClick={handleExit}>
              <ArrowLeft className="w-4 h-4 mr-2" />
              Exit
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* Quick Stats */}
        <div className="grid md:grid-cols-3 gap-6 mb-8">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Today's Earnings</p>
                  <p className="text-3xl mt-2">฿{todayEarnings}</p>
                </div>
                <div className="bg-green-50 p-3 rounded-full">
                  <DollarSign className="w-6 h-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Completed Today</p>
                  <p className="text-3xl mt-2">{completedToday}</p>
                </div>
                <div className="bg-blue-50 p-3 rounded-full">
                  <Package className="w-6 h-6 text-blue-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Active Delivery</p>
                  <p className="text-3xl mt-2">{activeDelivery ? '1' : '0'}</p>
                </div>
                <div className="bg-orange-50 p-3 rounded-full">
                  <Clock className="w-6 h-6 text-orange-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Active Delivery */}
        {activeDelivery && (
          <Card className="mb-8 border-2 border-orange-500">
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Active Delivery</CardTitle>
                <Badge className="bg-orange-500">In Progress</Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <p className="text-sm text-gray-500">Order #{activeDelivery.orderNumber}</p>
                  <p className="font-semibold">Restaurant ID: {activeDelivery.restaurantId}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 mb-1">Customer</p>
                  <p className="font-semibold">Customer ID: {activeDelivery.customerId}</p>
                </div>
                <div className="flex items-start gap-2">
                  <MapPin className="w-4 h-4 mt-1 text-gray-500 flex-shrink-0" />
                  <p className="text-sm">{activeDelivery.deliveryAddress}</p>
                </div>
                <Button 
                  className="w-full"
                  onClick={() => navigate(`/rider/delivery/${activeDelivery.id}`)}
                >
                  View Delivery Details
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Available Orders */}
        <Card>
          <CardHeader>
            <CardTitle>Available Orders Nearby</CardTitle>
          </CardHeader>
          <CardContent>
            {availableOrdersContent}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}