import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { InputOTP, InputOTPGroup, InputOTPSlot } from '../../components/ui/input-otp';
import { useApp } from '../../contexts/AppContext';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';

export default function CustomerLogin() {
  const navigate = useNavigate();
  const { setUser } = useApp();
  const [step, setStep] = useState<'credentials' | 'otp'>('credentials');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');

  const handleLogin = (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    // Simulate login - move to OTP step
    setStep('otp');
    toast.success('OTP sent to your email');
  };

  const handleVerifyOTP = () => {
    if (otp.length === 6) {
      // Simulate successful login
      setUser({
        id: 'CUST001',
        role: 'customer',
        name: 'John Doe',
        email: email
      });
      toast.success('Login successful!');
      navigate('/customer/restaurants');
    } else {
      toast.error('Please enter a valid 6-digit OTP');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-blue-50 to-indigo-100">
      <Card className="w-full max-w-md">
        <CardHeader>
          <Button 
            variant="ghost" 
            size="sm" 
            className="w-fit mb-2"
            onClick={() => navigate('/')}
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back
          </Button>
          <CardTitle>Customer Login</CardTitle>
          <CardDescription>
            {step === 'credentials' 
              ? 'Enter your credentials to continue' 
              : 'Enter the OTP sent to your email'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {step === 'credentials' ? (
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="john@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <div>
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <p className="text-xs text-gray-500 mt-1">
                  Password must be updated every month
                </p>
              </div>
              <Button type="submit" className="w-full">
                Continue to OTP
              </Button>
              <Button 
                type="button" 
                variant="outline" 
                className="w-full"
                onClick={() => navigate('/customer/register')}
              >
                Create New Account
              </Button>
            </form>
          ) : (
            <div className="space-y-4">
              <div className="flex justify-center">
                <InputOTP 
                  maxLength={6} 
                  value={otp}
                  onChange={setOtp}
                >
                  <InputOTPGroup>
                    <InputOTPSlot index={0} />
                    <InputOTPSlot index={1} />
                    <InputOTPSlot index={2} />
                    <InputOTPSlot index={3} />
                    <InputOTPSlot index={4} />
                    <InputOTPSlot index={5} />
                  </InputOTPGroup>
                </InputOTP>
              </div>
              <p className="text-sm text-center text-gray-500">
                Demo: Any 6-digit code will work
              </p>
              <Button onClick={handleVerifyOTP} className="w-full">
                Verify & Login
              </Button>
              <Button 
                variant="outline" 
                className="w-full"
                onClick={() => setStep('credentials')}
              >
                Back to Login
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
