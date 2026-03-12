import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { RadioGroup, RadioGroupItem } from '../../components/ui/radio-group';
import { Separator } from '../../components/ui/separator';
import { useApp } from '../../contexts/AppContext';
import { ArrowLeft, Trash2, Plus, Minus, CreditCard, QrCode, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import orderService, { CreateOrderRequest } from '../../services/order.service';

export default function Checkout() {
  const navigate = useNavigate();
  const { cart, updateCartQuantity, removeFromCart, clearCart, user } = useApp();
  const [paymentMethod, setPaymentMethod] = useState<'credit-card' | 'qr-code'>('credit-card');
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [placing, setPlacing] = useState(false);

  const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const deliveryFee = subtotal >= 300 ? 0 : 35; // matches backend calculateDeliveryFee()
  const total = subtotal + deliveryFee;

  const handlePlaceOrder = async () => {
    if (cart.length === 0) {
      toast.error('Your cart is empty');
      return;
    }

    if (!deliveryAddress.trim()) {
      toast.error('Please enter a delivery address');
      return;
    }

    const restaurantId = cart[0].restaurantId;

    const orderData: CreateOrderRequest = {
      customerId: user?.id?.toString() || '',
      restaurantId,
      deliveryAddress: deliveryAddress.trim(),
      deliveryLatitude: 13.7563,
      deliveryLongitude: 100.5018,
      orderItems: cart.map(item => ({
        menuItemId: item.id,
        menuItemName: item.name,
        quantity: item.quantity,
        unitPrice: item.price,
      })),
    };

    setPlacing(true);
    try {
      await orderService.createOrder(orderData);
      clearCart();
      toast.success('Order placed successfully!');
      navigate('/customer/orders');
    } catch {
      toast.error('Failed to place order. Please try again.');
    } finally {
      setPlacing(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <Button variant="ghost" onClick={() => navigate('/customer/restaurants')}>
            <ArrowLeft className="w-4 h-4 mr-2" />
            Continue Shopping
          </Button>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        <h1 className="text-3xl mb-8">Checkout</h1>

        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            {/* Cart Items */}
            <Card>
              <CardHeader>
                <CardTitle>Your Order</CardTitle>
              </CardHeader>
              <CardContent>
                {cart.length === 0 ? (
                  <div className="text-center py-8">
                    <p className="text-gray-500">Your cart is empty</p>
                    <Button 
                      onClick={() => navigate('/customer/restaurants')}
                      className="mt-4"
                    >
                      Browse Restaurants
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {cart.map(item => (
                      <div key={item.id} className="flex items-center gap-4 pb-4 border-b last:border-b-0">
                        <div className="flex-1">
                          <h3 className="font-semibold">{item.name}</h3>
                          <p className="text-sm text-gray-500">{item.restaurantName}</p>
                          <p className="text-sm font-semibold text-blue-600 mt-1">
                            ฿{item.price} × {item.quantity}
                          </p>
                        </div>
                        <div className="flex items-center gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => updateCartQuantity(item.id, item.quantity - 1)}
                          >
                            <Minus className="w-4 h-4" />
                          </Button>
                          <span className="w-8 text-center">{item.quantity}</span>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => updateCartQuantity(item.id, item.quantity + 1)}
                          >
                            <Plus className="w-4 h-4" />
                          </Button>
                        </div>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => removeFromCart(item.id)}
                        >
                          <Trash2 className="w-4 h-4 text-red-500" />
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Delivery Address */}
            <Card>
              <CardHeader>
                <CardTitle>Delivery Address</CardTitle>
              </CardHeader>
              <CardContent>
                <div>
                  <Label htmlFor="address">Address</Label>
                  <Input
                    id="address"
                    value={deliveryAddress}
                    onChange={(e) => setDeliveryAddress(e.target.value)}
                    placeholder="Enter your delivery address"
                  />
                </div>
              </CardContent>
            </Card>

            {/* Payment Method */}
            <Card>
              <CardHeader>
                <CardTitle>Payment Method</CardTitle>
              </CardHeader>
              <CardContent>
                <RadioGroup value={paymentMethod} onValueChange={(value: any) => setPaymentMethod(value)}>
                  <div className="flex items-center space-x-2 p-4 border rounded-lg cursor-pointer hover:bg-gray-50">
                    <RadioGroupItem value="credit-card" id="credit-card" />
                    <Label htmlFor="credit-card" className="flex-1 cursor-pointer flex items-center gap-2">
                      <CreditCard className="w-4 h-4" />
                      <div>
                        <p className="font-semibold">Credit Card</p>
                        <p className="text-sm text-gray-500">Pay with your saved credit card</p>
                      </div>
                    </Label>
                  </div>
                  <div className="flex items-center space-x-2 p-4 border rounded-lg cursor-pointer hover:bg-gray-50">
                    <RadioGroupItem value="qr-code" id="qr-code" />
                    <Label htmlFor="qr-code" className="flex-1 cursor-pointer flex items-center gap-2">
                      <QrCode className="w-4 h-4" />
                      <div>
                        <p className="font-semibold">QR Code / PromptPay</p>
                        <p className="text-sm text-gray-500">Scan QR code to pay</p>
                      </div>
                    </Label>
                  </div>
                </RadioGroup>
              </CardContent>
            </Card>
          </div>

          {/* Order Summary */}
          <div>
            <Card className="sticky top-4">
              <CardHeader>
                <CardTitle>Order Summary</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span>Subtotal</span>
                    <span>฿{subtotal.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Delivery Fee{subtotal >= 300 ? ' (Free)' : ''}</span>
                    <span>{subtotal >= 300 ? '฿0.00' : '฿35.00'}</span>
                  </div>
                  <Separator />
                  <div className="flex justify-between font-semibold">
                    <span>Total</span>
                    <span>฿{total.toFixed(2)}</span>
                  </div>
                </div>
                <Button 
                  className="w-full" 
                  onClick={handlePlaceOrder}
                  disabled={cart.length === 0 || placing}
                >
                  {placing ? (
                    <><Loader2 className="w-4 h-4 mr-2 animate-spin" /> Placing Order...</>
                  ) : 'Place Order'}
                </Button>
                <p className="text-xs text-gray-500 text-center">
                  By placing an order, you agree to our terms and conditions
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
