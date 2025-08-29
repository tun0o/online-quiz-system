import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { quizService } from '../../services/quizService';
import { Edit, Trash2, Search, Clock, CheckCircle, XCircle } from 'lucide-react';
import ConfirmationModal from '../common/ConfirmationModal';

export default function AllSubmissionsTable() {
  const [quizzes, setQuizzes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ title: '', subject: '', status: '' });
  const [pagination, setPagination] = useState({ page: 0, size: 10, totalPages: 0 });
  const [quizToDelete, setQuizToDelete] = useState(null);
  const navigate = useNavigate();

  const loadQuizzes = useCallback(async (page = 0) => {
    setLoading(true);
    try {
      const params = { page, size: pagination.size, ...filters };
      Object.keys(params).forEach(key => (params[key] === '' || params[key] === null) && delete params[key]);
      const response = await quizService.searchSubmissions(params);
      setQuizzes(response.content || []);
      setPagination(prev => ({ ...prev, page: response.number || 0, totalPages: response.totalPages || 0 }));
    } catch (error) {
      toast.error(`Không thể tải danh sách đề thi: ${error.message}`);
      setQuizzes([]); // Xóa danh sách khi có lỗi
    } finally {
      setLoading(false);
    }
  }, [filters, pagination.size]);

  useEffect(() => {
    loadQuizzes();
  }, [loadQuizzes]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    loadQuizzes(0);
  };

  const handleEdit = (quizId) => {
    navigate(`/admin/management/edit/${quizId}`);
  };

  const handleDeleteRequest = (quiz) => {
    setQuizToDelete(quiz);
  };

  const performDelete = async () => {
    if (!quizToDelete) return;
    try {
      await quizService.deleteSubmission(quizToDelete.id);
      toast.success('Đã xóa đề thi thành công!');
      loadQuizzes(pagination.page);
    } catch (error) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      setQuizToDelete(null);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      PENDING: 'bg-yellow-100 text-yellow-800 border-yellow-200',
      APPROVED: 'bg-green-100 text-green-800 border-green-200',
      REJECTED: 'bg-red-100 text-red-800 border-red-200'
    };
    const icons = {
      PENDING: <Clock size={14} />,
      APPROVED: <CheckCircle size={14} />,
      REJECTED: <XCircle size={14} />
    };
    const labels = { PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối' };
    return (
      <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium border ${styles[status]}`}>
        {icons[status]}
        {labels[status]}
      </span>
    );
  };

  const formatDate = (dateString) => new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  });

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 className="text-xl font-semibold mb-4">Quản lý Đề thi</h2>
      
      <form onSubmit={handleSearch} className="flex flex-wrap gap-4 mb-4 items-end">
        <input 
          type="text" 
          name="title" 
          value={filters.title} 
          onChange={handleFilterChange} 
          placeholder="Tìm theo tiêu đề..." 
          className="w-full sm:w-auto flex-grow p-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" 
        />
        <select 
          name="subject" 
          value={filters.subject} 
          onChange={handleFilterChange} 
          className="p-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Tất cả môn</option>
          <option value="MATH">Toán học</option>
          <option value="PHYSICS">Vật lý</option>
          <option value="CHEMISTRY">Hóa học</option>
          <option value="BIOLOGY">Sinh học</option>
          <option value="LITERATURE">Ngữ văn</option>
          <option value="ENGLISH">Tiếng Anh</option>
        </select>
        <select 
          name="status" 
          value={filters.status} 
          onChange={handleFilterChange} 
          className="p-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="PENDING">Chờ duyệt</option>
          <option value="APPROVED">Đã duyệt</option>
          <option value="REJECTED">Từ chối</option>
        </select>
        <button type="submit" className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
          <Search size={20} />
          Tìm kiếm
        </button>
      </form>

      <div className="overflow-x-auto">
        <table className="min-w-full bg-white text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="py-2 px-4 border-b text-left">Tiêu đề</th>
              <th className="py-2 px-4 border-b text-left">Môn học</th>
              <th className="py-2 px-4 border-b text-left">Người đóng góp</th>
              <th className="py-2 px-4 border-b text-left">Trạng thái</th>
              <th className="py-2 px-4 border-b text-left">Ngày tạo</th>
              <th className="py-2 px-4 border-b text-left">Hành động</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="6" className="text-center p-8 text-gray-500">Đang tải...</td></tr>
            ) : quizzes.length > 0 ? (
              quizzes.map(quiz => (
                <tr key={quiz.id} className="hover:bg-gray-50">
                  <td className="py-2 px-4 border-b">{quiz.title}</td>
                  <td className="py-2 px-4 border-b">{quiz.subject}</td>
                  <td className="py-2 px-4 border-b">{quiz.contributor?.name || 'N/A'}</td>
                  <td className="py-2 px-4 border-b">{getStatusBadge(quiz.status)}</td>
                  <td className="py-2 px-4 border-b">{formatDate(quiz.createdAt)}</td>
                  <td className="py-2 px-4 border-b flex gap-2">
                    <button onClick={() => handleEdit(quiz.id)} className="p-1.5 rounded-md text-blue-600 hover:bg-blue-100 hover:text-blue-700 transition" title="Sửa">
                      <Edit size={16} />
                    </button>
                    <button onClick={() => handleDeleteRequest(quiz)} className="p-1.5 rounded-md text-red-600 hover:bg-red-100 hover:text-red-700 transition" title="Xóa">
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr><td colSpan="6" className="text-center p-8 text-gray-500">Không tìm thấy đề thi nào.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {pagination.totalPages > 1 && (
        <div className="flex justify-center items-center gap-2 mt-6">
          {Array.from({ length: pagination.totalPages }, (_, i) => (
            <button
              key={i}
              onClick={() => loadQuizzes(i)}
              className={`px-3 py-1 rounded ${i === pagination.page ? 'bg-green-600 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'}`}
            >
              {i + 1}
            </button>
          ))}
        </div>
      )}

      <ConfirmationModal
        isOpen={!!quizToDelete}
        onClose={() => setQuizToDelete(null)}
        onConfirm={performDelete}
        title="Xác nhận xóa"
        message={`Bạn có chắc chắn muốn xóa đề thi "${quizToDelete?.title}" không? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="danger"
      />
    </div>
  );
}