import { useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { CheckCircle, XCircle } from 'lucide-react';

export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const vnp_ResponseCode = searchParams.get('vnp_ResponseCode');
  const isSuccess = vnp_ResponseCode === '00';

  return (
    <div className="text-center bg-white p-10 rounded-lg shadow-lg">
      {isSuccess ? (
        <>
          <CheckCircle size={64} className="mx-auto text-green-500" />
          <h1 className="text-2xl font-bold mt-4">Giao dịch thành công!</h1>
          <p className="text-gray-600 mt-2">Điểm đã được cộng vào tài khoản của bạn.</p>
        </>
      ) : (
        <>
          <XCircle size={64} className="mx-auto text-red-500" />
          <h1 className="text-2xl font-bold mt-4">Giao dịch thất bại</h1>
          <p className="text-gray-600 mt-2">Đã có lỗi xảy ra trong quá trình thanh toán.</p>
        </>
      )}
      <Link to="/" className="mt-6 inline-block bg-blue-500 text-white px-6 py-2 rounded">
        Về trang chủ
      </Link>
    </div>
  );
}
