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
    <div className="bg-gray-50 p-8 rounded-xl shadow-lg max-w-4xl mx-auto my-8">
      <h1 className="text-3xl font-extrabold text-gray-900 mb-8 text-center">Mua điểm tiêu dùng</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {pointPackages.map((pkg) => (
          <div key={pkg.id} className="bg-white border border-gray-200 rounded-xl p-8 text-center flex flex-col shadow-md hover:shadow-lg transition-all duration-300 ease-in-out transform hover:-translate-y-1">
            <h3 className="text-2xl font-bold text-gray-800 mb-4">{pkg.description}</h3>
            <p className="text-5xl font-extrabold my-4 text-indigo-600">{pkg.points} điểm</p>
            <p className="text-2xl font-semibold text-gray-700 mb-6">{pkg.price.toLocaleString('vi-VN')} VNĐ</p>
            <button
              onClick={() => handlePurchase(pkg.id)}
              disabled={loadingPackageId === pkg.id}
              className="mt-auto w-full bg-indigo-600 text-white py-3 rounded-lg hover:bg-indigo-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-50"
            >
              {loadingPackageId === pkg.id ? 'Đang xử lý...' : 'Mua ngay'} 🚀
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
