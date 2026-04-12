import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { format } from 'date-fns';
import {
  ArrowLeft,
  Bike,
  CheckCircle,
  ChefHat,
  Loader2,
  MapPin,
  Package,
  Route,
  Store,
  Star,
  Timer,
} from 'lucide-react';
import { Badge } from '../../components/ui/badge';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '../../components/ui/dialog';
import { Progress } from '../../components/ui/progress';
import { Textarea } from '../../components/ui/textarea';
import { useApp } from '../../contexts/AppContext';
import orderService, { Order, OrderStatus } from '../../services/order.service';
import restaurantService from '../../services/restaurant.service';
import { toast } from 'sonner';

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

type Point = {
  lat: number;
  lng: number;
};

type TrackingSnapshot = {
  restaurant: Point;
  customer: Point;
  rider: Point;
  progress: number;
  remainingKm: number;
  etaMinutes: number;
  phaseLabel: string;
  arrived: boolean;
};

const DELIVERY_SPEED_KMH = 24;
const MOCK_UPDATE_MS = 5000;
const ARRIVAL_RADIUS_KM = 0.15;

function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max);
}

function roundCoord(value: number) {
  return Number(value.toFixed(5));
}

function getSeed(order: Order) {
  const numericId = typeof order.id === 'number' ? order.id : Number(order.id);
  return Number.isFinite(numericId) ? numericId : String(order.id).length;
}

function getEffectiveStatus(order: Order, overrides: Record<string, OrderStatus>) {
  return overrides[String(order.id)] ?? order.status;
}

function createTrackingSnapshot(order: Order, step: number, status: OrderStatus): TrackingSnapshot {
  const seed = getSeed(order);
  const customer = {
    lat: order.deliveryLatitude ?? 13.7563 + seed * 0.0012,
    lng: order.deliveryLongitude ?? 100.5018 + seed * 0.0011,
  };
  const restaurant = {
    lat: roundCoord(customer.lat - (0.018 + (seed % 4) * 0.004)),
    lng: roundCoord(customer.lng - (0.012 + (seed % 3) * 0.003)),
  };

  if (status === OrderStatus.DELIVERED) {
    return {
      restaurant,
      customer,
      rider: customer,
      progress: 1,
      remainingKm: 0,
      etaMinutes: 0,
      phaseLabel: 'Delivered',
      arrived: true,
    };
  }

  const totalSteps = 6;
  const normalizedStep = clamp(step, 0, totalSteps);
  const progress = status === OrderStatus.PICKED_UP
    ? clamp(0.2 + normalizedStep * 0.14, 0.2, 0.98)
    : 0;

  const rider = {
    lat: roundCoord(restaurant.lat + (customer.lat - restaurant.lat) * progress),
    lng: roundCoord(restaurant.lng + (customer.lng - restaurant.lng) * progress),
  };

  const totalKm = calculateDistanceKm(restaurant, customer);
  const remainingKm = Math.max(totalKm * (1 - progress), 0);
  const etaMinutes = Math.max(Math.ceil((remainingKm / DELIVERY_SPEED_KMH) * 60), 0);
  const arrived = remainingKm <= ARRIVAL_RADIUS_KM;

  let phaseLabel = 'Waiting for pickup';
  if (status === OrderStatus.PICKED_UP) {
    phaseLabel = progress < 0.45 ? 'Rider just picked up your order' :
      progress < 0.8 ? 'Rider is on the way' :
      arrived ? 'Rider arrived at your address' :
      'Rider is nearby';
  }

  return {
    restaurant,
    customer,
    rider,
    progress,
    remainingKm: Number(remainingKm.toFixed(2)),
    etaMinutes,
    phaseLabel,
    arrived,
  };
}

function calculateDistanceKm(start: Point, end: Point) {
  const earthRadiusKm = 6371;
  const dLat = ((end.lat - start.lat) * Math.PI) / 180;
  const dLng = ((end.lng - start.lng) * Math.PI) / 180;
  const lat1 = (start.lat * Math.PI) / 180;
  const lat2 = (end.lat * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return earthRadiusKm * c;
}

function toMapPosition(point: Point, bounds: { minLat: number; maxLat: number; minLng: number; maxLng: number }) {
  const x = ((point.lng - bounds.minLng) / (bounds.maxLng - bounds.minLng || 1)) * 100;
  const y = ((bounds.maxLat - point.lat) / (bounds.maxLat - bounds.minLat || 1)) * 100;
  return {
    left: `${clamp(x, 6, 94)}%`,
    top: `${clamp(y, 8, 92)}%`,
  };
}

function DeliveryMap({ snapshot }: { snapshot: TrackingSnapshot }) {
  const padding = 0.01;
  const bounds = {
    minLat: Math.min(snapshot.restaurant.lat, snapshot.customer.lat, snapshot.rider.lat) - padding,
    maxLat: Math.max(snapshot.restaurant.lat, snapshot.customer.lat, snapshot.rider.lat) + padding,
    minLng: Math.min(snapshot.restaurant.lng, snapshot.customer.lng, snapshot.rider.lng) - padding,
    maxLng: Math.max(snapshot.restaurant.lng, snapshot.customer.lng, snapshot.rider.lng) + padding,
  };

  const restaurantPos = toMapPosition(snapshot.restaurant, bounds);
  const customerPos = toMapPosition(snapshot.customer, bounds);
  const riderPos = toMapPosition(snapshot.rider, bounds);

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 text-slate-900 shadow-sm">
      <div className="mb-3 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold">Mock Live Tracking Map</p>
          <p className="text-xs text-slate-500">Updates every 5 seconds with simulated GPS coordinates</p>
        </div>
        <Badge className="bg-emerald-500 text-white">{snapshot.phaseLabel}</Badge>
      </div>

      <div className="relative h-72 overflow-hidden rounded-xl border border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.10),_transparent_24%),linear-gradient(180deg,_#f8fbff,_#eef5ff)]">
        <div className="absolute inset-0 bg-[linear-gradient(rgba(148,163,184,0.12)_1px,transparent_1px),linear-gradient(90deg,rgba(148,163,184,0.12)_1px,transparent_1px)] bg-[size:32px_32px]" />
        <div className="absolute inset-x-0 top-0 h-16 bg-gradient-to-b from-white/60 to-transparent" />
        <svg className="absolute inset-0 h-full w-full">
          <line
            x1={restaurantPos.left}
            y1={restaurantPos.top}
            x2={customerPos.left}
            y2={customerPos.top}
            stroke="rgba(59,130,246,0.28)"
            strokeDasharray="8 8"
            strokeWidth="4"
          />
          <line
            x1={restaurantPos.left}
            y1={restaurantPos.top}
            x2={riderPos.left}
            y2={riderPos.top}
            stroke="rgba(34,197,94,0.92)"
            strokeWidth="5"
          />
        </svg>

        <div className="absolute -translate-x-1/2 -translate-y-1/2" style={restaurantPos}>
          <div className="rounded-full bg-amber-400 p-2 text-slate-900 shadow-lg shadow-amber-400/25 ring-4 ring-white">
            <Store className="h-4 w-4" />
          </div>
          <p className="mt-2 text-xs font-medium text-slate-700">Restaurant</p>
        </div>

        <div className="absolute -translate-x-1/2 -translate-y-1/2" style={customerPos}>
          <div className="rounded-full bg-rose-500 p-2 text-white shadow-lg shadow-rose-500/25 ring-4 ring-white">
            <MapPin className="h-4 w-4" />
          </div>
          <p className="mt-2 text-xs font-medium text-slate-700">Your Address</p>
        </div>

        <div className="absolute -translate-x-1/2 -translate-y-1/2 transition-all duration-1000" style={riderPos}>
          <div className="rounded-full bg-sky-400 p-2 text-slate-950 shadow-lg shadow-sky-400/30 ring-4 ring-white">
            <Bike className="h-4 w-4" />
          </div>
          <p className="mt-2 text-xs font-medium text-slate-700">Rider</p>
        </div>
      </div>

      <div className="mt-4 grid gap-3 md:grid-cols-3">
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
          <p className="text-xs uppercase tracking-wide text-slate-500">Restaurant GPS</p>
          <p className="mt-1 text-sm font-semibold">{snapshot.restaurant.lat}, {snapshot.restaurant.lng}</p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
          <p className="text-xs uppercase tracking-wide text-slate-500">Rider GPS</p>
          <p className="mt-1 text-sm font-semibold">{snapshot.rider.lat}, {snapshot.rider.lng}</p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
          <p className="text-xs uppercase tracking-wide text-slate-500">Destination GPS</p>
          <p className="mt-1 text-sm font-semibold">{snapshot.customer.lat}, {snapshot.customer.lng}</p>
        </div>
      </div>
    </div>
  );
}

function StarPicker({
  value,
  onChange,
  disabled = false,
}: {
  value: number;
  onChange: (value: number) => void;
  disabled?: boolean;
}) {
  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((starValue) => (
        <button
          key={starValue}
          type="button"
          disabled={disabled}
          className="rounded-md p-1 transition hover:scale-105 disabled:cursor-not-allowed disabled:opacity-60"
          onClick={() => onChange(starValue)}
          aria-label={`Rate ${starValue} star${starValue > 1 ? 's' : ''}`}
        >
          <Star
            className={`h-5 w-5 ${starValue <= value ? 'fill-yellow-400 text-yellow-400' : 'text-slate-300'}`}
          />
        </button>
      ))}
    </div>
  );
}

function ReviewComposer({
  order,
  customerId,
  onSubmitted,
}: {
  order: Order;
  customerId: string;
  onSubmitted: (updatedOrder: Order) => void;
}) {
  const [rating, setRating] = useState(order.restaurantRating ?? 5);
  const [reviewText, setReviewText] = useState(order.restaurantReviewText ?? '');
  const [submitting, setSubmitting] = useState(false);

  const canReview = [OrderStatus.DELIVERED, 'COMPLETED'].includes(order.status);
  const alreadyReviewed = Boolean(order.restaurantReviewId);

  const handleSubmit = async () => {
    if (!canReview || alreadyReviewed) {
      return;
    }

    setSubmitting(true);
    try {
      const review = await restaurantService.submitRestaurantReview(String(order.restaurantId), {
        orderId: String(order.id),
        customerId,
        rating,
        reviewText: reviewText.trim() || undefined,
      });

      onSubmitted({
        ...order,
        restaurantReviewId: Number(review.id),
        restaurantRating: review.rating,
        restaurantReviewText: review.reviewText,
        restaurantReviewedAt: review.createdAt,
      });
      toast.success('Thanks for rating this restaurant.');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to submit review';
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  if (alreadyReviewed) {
    return (
      <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-4">
        <p className="text-sm font-semibold text-emerald-900">Your review has been submitted</p>
        <div className="mt-2 flex items-center gap-2 text-sm text-emerald-900">
          <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
          <span>{order.restaurantRating}/5</span>
        </div>
        {order.restaurantReviewText && (
          <p className="mt-2 text-sm text-emerald-800">{order.restaurantReviewText}</p>
        )}
      </div>
    );
  }

  if (!canReview) {
    return null;
  }

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-slate-900">Rate this order</p>
          <p className="text-sm text-slate-500">Your feedback updates the restaurant rating right away.</p>
        </div>
        <StarPicker value={rating} onChange={setRating} disabled={submitting} />
      </div>

      <Textarea
        className="mt-3 min-h-24"
        value={reviewText}
        onChange={(event) => setReviewText(event.target.value)}
        placeholder="Share anything the restaurant did well, or what could be better."
        disabled={submitting}
      />

      <div className="mt-3 flex justify-end">
        <Button onClick={handleSubmit} disabled={submitting}>
          {submitting ? 'Submitting...' : 'Submit Review'}
        </Button>
      </div>
    </div>
  );
}

export default function OrderTracking() {
  const navigate = useNavigate();
  const { user } = useApp();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null);
  const [deliverySteps, setDeliverySteps] = useState<Record<string, number>>({});
  const [statusOverrides, setStatusOverrides] = useState<Record<string, OrderStatus>>({});

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

  const selectedOrder = orders.find(order => String(order.id) === selectedOrderId) ?? null;
  const selectedStatus = selectedOrder ? getEffectiveStatus(selectedOrder, statusOverrides) : null;
  const selectedSnapshot = selectedOrder && selectedStatus
    ? createTrackingSnapshot(selectedOrder, deliverySteps[String(selectedOrder.id)] ?? 0, selectedStatus)
    : null;

  useEffect(() => {
    if (!selectedOrder || selectedStatus !== OrderStatus.PICKED_UP) {
      return undefined;
    }

    const orderKey = String(selectedOrder.id);
    const interval = window.setInterval(() => {
      setDeliverySteps(current => {
        const nextStep = Math.min((current[orderKey] ?? 0) + 1, 6);
        const nextSnapshot = createTrackingSnapshot(selectedOrder, nextStep, OrderStatus.PICKED_UP);

        if (nextSnapshot.arrived) {
          setStatusOverrides(statusMap => ({
            ...statusMap,
            [orderKey]: OrderStatus.DELIVERED,
          }));
        }

        return {
          ...current,
          [orderKey]: nextStep,
        };
      });
    }, MOCK_UPDATE_MS);

    return () => window.clearInterval(interval);
  }, [selectedOrder, selectedStatus]);

  const handleOrderReviewed = (updatedOrder: Order) => {
    setOrders((currentOrders) =>
      currentOrders.map((order) => (order.id === updatedOrder.id ? updatedOrder : order))
    );
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="bg-white shadow-sm">
        <div className="mx-auto max-w-4xl px-4 py-4">
          <div className="flex items-center justify-between">
            <Button variant="ghost" onClick={() => navigate('/customer/restaurants')}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Restaurants
            </Button>
            <h1 className="text-xl">My Orders</h1>
            <div className="w-24" />
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-4xl px-4 py-8">
        {loading && (
          <div className="flex justify-center py-16">
            <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
          </div>
        )}

        {!loading && orders.length === 0 && (
          <Card>
            <CardContent className="py-12 text-center">
              <Package className="mx-auto mb-4 h-16 w-16 text-gray-300" />
              <p className="mb-4 text-gray-500">No orders yet</p>
              <Button onClick={() => navigate('/customer/restaurants')}>
                Start Ordering
              </Button>
            </CardContent>
          </Card>
        )}

        {!loading && orders.length > 0 && (
          <div className="space-y-6">
            {orders.map(order => {
              const effectiveStatus = getEffectiveStatus(order, statusOverrides);
              const config = statusConfig[effectiveStatus] ?? statusConfig[OrderStatus.PENDING];
              const Icon = config.icon;
              const snapshot = createTrackingSnapshot(order, deliverySteps[String(order.id)] ?? 0, effectiveStatus);
              const isLiveTracking = effectiveStatus === OrderStatus.PICKED_UP || effectiveStatus === OrderStatus.DELIVERED;

              return (
                <Card key={order.id}>
                  <CardHeader>
                    <div className="flex items-center justify-between gap-4">
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
                    <div>
                      <div className="mb-2 flex items-center gap-2">
                        <Icon className="h-4 w-4" />
                        <span className="text-sm font-semibold">{config.label}</span>
                      </div>
                      <Progress value={config.progress} />
                    </div>

                    {isLiveTracking && (
                      <div className="grid gap-3 rounded-xl border bg-slate-50 p-4 md:grid-cols-3">
                        <div className="flex items-start gap-3">
                          <Timer className="mt-0.5 h-4 w-4 text-indigo-600" />
                          <div>
                            <p className="text-xs uppercase tracking-wide text-gray-500">ETA</p>
                            <p className="text-sm font-semibold">
                              {snapshot.etaMinutes === 0 ? 'Arrived' : `${snapshot.etaMinutes} min`}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-start gap-3">
                          <Route className="mt-0.5 h-4 w-4 text-indigo-600" />
                          <div>
                            <p className="text-xs uppercase tracking-wide text-gray-500">Remaining Distance</p>
                            <p className="text-sm font-semibold">{snapshot.remainingKm.toFixed(2)} km</p>
                          </div>
                        </div>
                        <div className="flex items-start gap-3">
                          <Bike className="mt-0.5 h-4 w-4 text-indigo-600" />
                          <div>
                            <p className="text-xs uppercase tracking-wide text-gray-500">Live Status</p>
                            <p className="text-sm font-semibold">{snapshot.phaseLabel}</p>
                          </div>
                        </div>
                      </div>
                    )}

                    <div className="space-y-2">
                      <p className="text-sm font-semibold">Items:</p>
                      {(order.orderItems ?? []).map(item => (
                        <div key={item.id} className="flex justify-between text-sm">
                          <span>{item.menuItemName} × {item.quantity}</span>
                          <span>฿{item.totalPrice.toFixed(2)}</span>
                        </div>
                      ))}
                    </div>

                    <div className="border-t pt-4">
                      <p className="text-sm text-gray-600">{order.deliveryAddress}</p>
                    </div>

                    <ReviewComposer
                      order={order}
                      customerId={String(user?.id ?? '')}
                      onSubmitted={handleOrderReviewed}
                    />

                    <div className="flex items-center justify-between border-t pt-2">
                      <div>
                        <p className="text-sm text-gray-500">Total Amount</p>
                        <p className="text-lg font-semibold">฿{(order.totalAmount ?? 0).toFixed(2)}</p>
                      </div>
                      <Button variant="outline" onClick={() => setSelectedOrderId(String(order.id))}>
                        View Details
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>

      <Dialog open={selectedOrderId !== null} onOpenChange={(open) => !open && setSelectedOrderId(null)}>
        <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-4xl">
          <DialogHeader>
            <DialogTitle>
              {selectedOrder ? `Order Details #${selectedOrder.orderNumber}` : 'Order Details'}
            </DialogTitle>
            <DialogDescription>
              Review order items, delivery address, live tracking, and leave feedback after delivery.
            </DialogDescription>
          </DialogHeader>

          {selectedOrder && selectedStatus && selectedSnapshot && (
            <div className="space-y-6">
              <div className="grid gap-4 rounded-2xl border bg-slate-50 p-4 md:grid-cols-4">
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500">Status</p>
                  <p className="mt-1 text-sm font-semibold">{statusConfig[selectedStatus].label}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500">Ordered At</p>
                  <p className="mt-1 text-sm font-semibold">{format(new Date(selectedOrder.createdAt), 'PPp')}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500">ETA</p>
                  <p className="mt-1 text-sm font-semibold">
                    {selectedSnapshot.etaMinutes === 0 ? 'Arrived' : `${selectedSnapshot.etaMinutes} min`}
                  </p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500">Route Distance</p>
                  <p className="mt-1 text-sm font-semibold">
                    {calculateDistanceKm(selectedSnapshot.restaurant, selectedSnapshot.customer).toFixed(2)} km
                  </p>
                </div>
              </div>

              {(selectedStatus === OrderStatus.PICKED_UP || selectedStatus === OrderStatus.DELIVERED) && (
                <DeliveryMap snapshot={selectedSnapshot} />
              )}

              <div className="grid gap-6 md:grid-cols-2">
                <div className="rounded-2xl border p-4">
                  <p className="text-sm font-semibold">Order Items</p>
                  <div className="mt-3 space-y-2">
                    {(selectedOrder.orderItems ?? []).map(item => (
                      <div key={item.id} className="flex items-center justify-between text-sm">
                        <span>{item.menuItemName} × {item.quantity}</span>
                        <span>฿{item.totalPrice.toFixed(2)}</span>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="rounded-2xl border p-4">
                  <p className="text-sm font-semibold">Delivery Details</p>
                  <div className="mt-3 space-y-3 text-sm text-gray-600">
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-500">Address</p>
                      <p className="mt-1 text-sm text-gray-900">{selectedOrder.deliveryAddress}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-500">Tracking Note</p>
                      <p className="mt-1 text-sm text-gray-900">{selectedSnapshot.phaseLabel}</p>
                    </div>
                    <div>
                      <p className="text-xs uppercase tracking-wide text-gray-500">Total Amount</p>
                      <p className="mt-1 text-lg font-semibold text-gray-900">
                        ฿{(selectedOrder.totalAmount ?? 0).toFixed(2)}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <ReviewComposer
                order={selectedOrder}
                customerId={String(user?.id ?? '')}
                onSubmitted={(updatedOrder) => {
                  handleOrderReviewed(updatedOrder);
                }}
              />

              <div className="flex justify-end">
                <Button variant="outline" onClick={() => setSelectedOrderId(null)}>
                  Close Details
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
