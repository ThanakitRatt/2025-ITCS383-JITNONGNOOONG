import { useParams, useNavigate } from 'react-router';
import { useEffect, useState } from 'react';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { useApp } from '../../contexts/AppContext';
import { ArrowLeft, MapPin, Phone, Navigation, CheckCircle, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { orderService, Order, OrderStatus } from '../../services/order.service';

export default function RiderDelivery() {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const { user } = useApp();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);

  // Fetch order details from API
  useEffect(() => {
    const fetchOrder = async () => {
      if (!orderId) {
        navigate('/rider/dashboard');
        return;
      }

      try {
        setLoading(true);
        const orderData = await orderService.getOrderById(orderId);
        setOrder(orderData);
      } catch (error) {
        console.error('Failed to fetch order:', error);
        toast.error('Failed to load order details');
        navigate('/rider/dashboard');
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId, navigate]);

  const handleCompleteDelivery = async () => {
    if (!order) return;
    
    try {
      // Update order status to DELIVERED
      await orderService.updateOrderStatus(order.id, {
        newStatus: OrderStatus.DELIVERED,
        updatedBy: Number(user?.id || 1),
        notes: 'Delivery completed by rider'
      });
      
      toast.success('Delivery completed! ฿50 earned');
      navigate('/rider/dashboard');
    } catch (error) {
      console.error('Failed to complete delivery:', error);
      toast.error('Failed to complete delivery. Please try again.');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardContent className="text-center py-8">
            <Loader2 className="w-8 h-8 animate-spin mx-auto mb-4 text-gray-400" />
            <p className="text-gray-500">Loading order details...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardContent className="text-center py-8">
            <p className="text-gray-500">Order not found</p>
            <Button onClick={() => navigate('/rider/dashboard')} className="mt-4">
              Back to Dashboard
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const getStatusBadge = () => {
    if (order.status === OrderStatus.PICKED_UP) {
      return <Badge className="bg-orange-500">Delivering</Badge>;
    }
    return <Badge className="bg-blue-500">{order.status}</Badge>;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl">Delivery Details</h1>
            <Button variant="ghost" onClick={() => navigate('/rider/dashboard')}>
              <ArrowLeft className="w-4 h-4 mr-2" />
              Dashboard
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="space-y-6">
          {/* Order Status */}
          <Card className="border-2 border-orange-500">
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Order #{order.orderNumber}</CardTitle>
                {getStatusBadge()}
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-center py-4">
                <p className="text-2xl font-semibold text-green-600 mb-2">฿{order.deliveryFee.toFixed(2)}</p>
                <p className="text-sm text-gray-500">Delivery Fee</p>
              </div>
            </CardContent>
          </Card>

          {/* Restaurant Info */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Pickup Location</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <p className="font-semibold">Restaurant ID: {order.restaurantId}</p>
              {order.orderItems && order.orderItems.length > 0 && (
                <div className="pt-3 border-t">
                  <p className="text-sm font-semibold mb-2">Order Items:</p>
                  {order.orderItems.map((item, idx) => (
                    <div key={item.menuItemId ?? idx} className="text-sm text-gray-600 mb-1">
                      <span className="font-medium">{item.menuItemName}</span> × {item.quantity}
                      <span className="float-right">฿{item.totalPrice.toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              )}
              {order.specialInstructions && (
                <div className="pt-3 border-t">
                  <p className="text-sm font-semibold mb-1">Special Instructions:</p>
                  <p className="text-sm text-gray-600">{order.specialInstructions}</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Customer Info */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Delivery Location</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-sm text-gray-500 mb-1">Customer ID</p>
                <p className="font-semibold">{order.customerId}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500 mb-1">Delivery Address</p>
                <div className="flex items-start gap-2">
                  <MapPin className="w-4 h-4 mt-1 text-gray-500 flex-shrink-0" />
                  <p className="text-sm">{order.deliveryAddress}</p>
                </div>
              </div>
              {!!(order.deliveryLatitude && order.deliveryLongitude) && (
                <div className="flex gap-2 pt-3 border-t">
                  <Button variant="outline" className="flex-1">
                    <Phone className="w-4 h-4 mr-2" />
                    Call Customer
                  </Button>
                  <Button 
                    variant="outline" 
                    className="flex-1"
                    onClick={() => window.open(
                      `https://www.google.com/maps/dir/?api=1&destination=${order.deliveryLatitude},${order.deliveryLongitude}`,
                      '_blank'
                    )}
                  >
                    <Navigation className="w-4 h-4 mr-2" />
                    Navigate
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Payment Info */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Order Summary</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-gray-600">Order Total:</span>
                  <span className="font-semibold">฿{order.totalAmount.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Delivery Fee:</span>
                  <span>฿{order.deliveryFee.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-sm pt-2 border-t font-semibold">
                  <span>Total Amount:</span>
                  <span>฿{order.totalAmount.toFixed(2)}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Complete Delivery Button */}
          {order.status === OrderStatus.PICKED_UP && (
            <Button 
              className="w-full h-14 text-lg"
              onClick={handleCompleteDelivery}
            >
              <CheckCircle className="w-5 h-5 mr-2" />
              Complete Delivery
            </Button>
          )}
          
          {order.status === OrderStatus.DELIVERED && (
            <Card className="border-2 border-green-500">
              <CardContent className="text-center py-6">
                <CheckCircle className="w-12 h-12 text-green-500 mx-auto mb-2" />
                <p className="text-lg font-semibold text-green-600">Delivery Completed!</p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
