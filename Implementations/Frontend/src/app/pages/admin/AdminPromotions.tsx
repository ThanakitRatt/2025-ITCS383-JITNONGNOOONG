import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Textarea } from '../../components/ui/textarea';
import { Badge } from '../../components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../../components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../../components/ui/select';
import { ArrowLeft, Plus, Trash2, Tag } from 'lucide-react';
import { toast } from 'sonner';

interface SystemPromotion {
  id: string;
  code: string;
  title: string;
  description: string;
  discountPercent: number;
  maxUses: number;
  currentUses: number;
  validFrom: string;
  validUntil: string;
  targetGroup: 'all' | 'new' | 'vip';
  active: boolean;
}

export default function AdminPromotions() {
  const navigate = useNavigate();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const defaultPromotions: SystemPromotion[] = [
    {
      id: 'SYSPROMO001',
      code: 'NEWUSER50',
      title: 'New User Welcome',
      description: 'Get 50% off your first order',
      discountPercent: 50,
      maxUses: 1000,
      currentUses: 342,
      validFrom: '2026-03-01',
      validUntil: '2026-03-31',
      targetGroup: 'new',
      active: true
    },
    {
      id: 'SYSPROMO002',
      code: 'WEEKEND20',
      title: 'Weekend Discount',
      description: '20% off on all orders during weekends',
      discountPercent: 20,
      maxUses: 5000,
      currentUses: 1823,
      validFrom: '2026-03-06',
      validUntil: '2026-03-07',
      targetGroup: 'all',
      active: true
    }
  ];
  const [promotions, setPromotions] = useState<SystemPromotion[]>(() => {
    try { const s = localStorage.getItem('admin_promotions'); return s ? JSON.parse(s) : defaultPromotions; } catch { return defaultPromotions; }
  });
  
  const [formData, setFormData] = useState({
    code: '',
    title: '',
    description: '',
    discountPercent: '',
    maxUses: '',
    validFrom: '',
    validUntil: '',
    targetGroup: 'all'
  });

  const updatePromotions = (next: SystemPromotion[]) => {
    localStorage.setItem('admin_promotions', JSON.stringify(next));
    setPromotions(next);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const newPromo: SystemPromotion = {
      id: `SYSPROMO${Date.now()}`,
      code: formData.code.toUpperCase(),
      title: formData.title,
      description: formData.description,
      discountPercent: parseFloat(formData.discountPercent),
      maxUses: parseInt(formData.maxUses),
      currentUses: 0,
      validFrom: formData.validFrom,
      validUntil: formData.validUntil,
      targetGroup: formData.targetGroup as any,
      active: true
    };
    
    updatePromotions([...promotions, newPromo]);
    setFormData({
      code: '',
      title: '',
      description: '',
      discountPercent: '',
      maxUses: '',
      validFrom: '',
      validUntil: '',
      targetGroup: 'all'
    });
    setIsDialogOpen(false);
    toast.success('System promotion created successfully');
  };

  const handleDelete = (id: string) => {
    updatePromotions(promotions.filter(p => p.id !== id));
    toast.success('Promotion deleted');
  };

  const toggleActive = (id: string) => {
    updatePromotions(promotions.map(p => p.id === id ? { ...p, active: !p.active } : p));
    toast.success('Promotion status updated');
  };

  const targetGroupLabels: Record<string, string> = {
    all: 'All Users',
    new: 'New Users',
    vip: 'VIP Members'
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl">System Promotions</h1>
            <div className="flex gap-2">
              <Button onClick={() => setIsDialogOpen(true)}>
                <Plus className="w-4 h-4 mr-2" />
                Create Promotion
              </Button>
              <Button variant="ghost" onClick={() => navigate('/admin/dashboard')}>
                <ArrowLeft className="w-4 h-4 mr-2" />
                Dashboard
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        {promotions.length === 0 ? (
          <Card>
            <CardContent className="text-center py-12">
              <Tag className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <p className="text-gray-500 mb-4">No system promotions yet</p>
              <Button onClick={() => setIsDialogOpen(true)}>
                <Plus className="w-4 h-4 mr-2" />
                Create Your First Promotion
              </Button>
            </CardContent>
          </Card>
        ) : (
          <div className="grid md:grid-cols-2 gap-6">
            {promotions.map(promo => (
              <Card key={promo.id}>
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <CardTitle className="text-lg">{promo.title}</CardTitle>
                        <Badge variant={promo.active ? 'default' : 'secondary'}>
                          {promo.active ? 'Active' : 'Inactive'}
                        </Badge>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge variant="outline" className="font-mono">
                          {promo.code}
                        </Badge>
                        <Badge variant="outline">
                          {targetGroupLabels[promo.targetGroup]}
                        </Badge>
                      </div>
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <p className="text-sm text-gray-600">{promo.description}</p>
                  
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm text-gray-500">Discount</p>
                      <p className="text-xl font-semibold text-green-600">
                        {promo.discountPercent}%
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Usage</p>
                      <p className="text-xl font-semibold">
                        {promo.currentUses} / {promo.maxUses}
                      </p>
                    </div>
                  </div>

                  <div className="text-sm">
                    <p className="text-gray-500">Valid Period</p>
                    <p className="font-semibold">
                      {promo.validFrom} to {promo.validUntil}
                    </p>
                  </div>

                  <div className="flex gap-2 pt-4 border-t">
                    <Button
                      variant="outline"
                      className="flex-1"
                      onClick={() => toggleActive(promo.id)}
                    >
                      {promo.active ? 'Deactivate' : 'Activate'}
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => handleDelete(promo.id)}
                    >
                      <Trash2 className="w-4 h-4 text-red-500" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Create Promotion Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Create System Promotion</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="code">Promotion Code *</Label>
              <Input
                id="code"
                value={formData.code}
                onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                placeholder="e.g., NEWUSER50"
                required
              />
              <p className="text-xs text-gray-500 mt-1">
                Code will be automatically converted to uppercase
              </p>
            </div>

            <div>
              <Label htmlFor="title">Title *</Label>
              <Input
                id="title"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="e.g., New User Welcome"
                required
              />
            </div>

            <div>
              <Label htmlFor="description">Description *</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Describe your promotion..."
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="discount">Discount Percentage *</Label>
                <Input
                  id="discount"
                  type="number"
                  min="1"
                  max="100"
                  value={formData.discountPercent}
                  onChange={(e) => setFormData({ ...formData, discountPercent: e.target.value })}
                  placeholder="e.g., 20"
                  required
                />
              </div>
              <div>
                <Label htmlFor="maxUses">Maximum Uses *</Label>
                <Input
                  id="maxUses"
                  type="number"
                  min="1"
                  value={formData.maxUses}
                  onChange={(e) => setFormData({ ...formData, maxUses: e.target.value })}
                  placeholder="e.g., 1000"
                  required
                />
              </div>
            </div>

            <div>
              <Label htmlFor="targetGroup">Target Group *</Label>
              <Select
                value={formData.targetGroup}
                onValueChange={(value) => setFormData({ ...formData, targetGroup: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Users</SelectItem>
                  <SelectItem value="new">New Users Only</SelectItem>
                  <SelectItem value="vip">VIP Members</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="validFrom">Valid From *</Label>
                <Input
                  id="validFrom"
                  type="date"
                  value={formData.validFrom}
                  onChange={(e) => setFormData({ ...formData, validFrom: e.target.value })}
                  required
                />
              </div>
              <div>
                <Label htmlFor="validUntil">Valid Until *</Label>
                <Input
                  id="validUntil"
                  type="date"
                  value={formData.validUntil}
                  onChange={(e) => setFormData({ ...formData, validUntil: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="flex gap-2 pt-4">
              <Button 
                type="button" 
                variant="outline" 
                className="flex-1"
                onClick={() => setIsDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button type="submit" className="flex-1">
                Create Promotion
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
