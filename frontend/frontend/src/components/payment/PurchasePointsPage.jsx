import { useState } from 'react';
import { paymentService } from '@/services/paymentService';
import { toast } from 'react-toastify';

const pointPackages = [
  { id: 1, points: 200, price: 20000, description: "Gói khởi đầu" },
  { id: 2, points: 1000, price: 90000, description: "Gói tiết kiệm" },
  { id: 3, points: 2500, price: 200000, description: "Gói chuyên nghiệp" },
];

export default function PurchasePointsPage() {
  const [loadingPackageId, setLoadingPackageId] = useState(null);

  const handlePurchase = async (packageId) => {
    setLoadingPackageId(packageId);
    try {
      const response = await paymentService.createPayment({ packageId });
      // Nhận được URL thanh toán, chuyển hướng người dùng
      window.location.href = response.paymentUrl;
    } catch (error) {
      toast.error("Không thể tạo giao dịch. Vui lòng thử lại.");
      setLoadingPackageId(null);
    }
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <h1 className="text-2xl font-bold mb-6">Mua điểm tiêu dùng</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {pointPackages.map((pkg) => (
          <div key={pkg.id} className="border rounded-lg p-6 text-center flex flex-col">
            <h3 className="text-xl font-semibold">{pkg.description}</h3>
            <p className="text-4xl font-bold my-4 text-green-600">{pkg.points} điểm</p>
            <p className="text-lg text-gray-700 mb-6">{pkg.price.toLocaleString('vi-VN')} VNĐ</p>
            <button
              onClick={() => handlePurchase(pkg.id)}
              disabled={loadingPackageId === pkg.id}
              className="mt-auto w-full bg-green-600 text-white py-2 rounded-lg hover:bg-green-700 disabled:bg-gray-400"
            >
              {loadingPackageId === pkg.id ? 'Đang xử lý...' : 'Mua ngay'}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
