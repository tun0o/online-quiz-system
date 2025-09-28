import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { quizService } from '@/services/quizService';
import { Edit, Trash2, BookOpen } from 'lucide-react';
import { subjectDisplayMap, difficultyDisplayMap, getDifficultyColor } from '@/utils/displayMaps';
import ConfirmationModal from '@/components/common/ConfirmationModal';

export default function AllSubmissionsTable() {
  const [quizzes, setQuizzes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ subject: '', difficulty: '' });
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [pagination, setPagination] = useState({ page: 0, size: 10, totalPages: 0 });
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [quizToDelete, setQuizToDelete] = useState(null);
  const navigate = useNavigate();

  // Debounce search term
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 500); // 500ms delay
    return () => clearTimeout(handler);
  }, [searchTerm]);

  const loadQuizzes = useCallback(async (pageToLoad) => {
    setLoading(true);
    try {
      const params = {
        page: pageToLoad ?? pagination.page,
        size: pagination.size,
        keyword: debouncedSearchTerm,
        subject: filters.subject,
        difficulty: filters.difficulty,
        status: 'APPROVED' // Chỉ tìm các đề đã duyệt
        // Đã loại bỏ logic sắp xếp
      };
      const response = await quizService.searchSubmissions(params);
      setQuizzes(response.content || []);
      setPagination(prev => ({ ...prev, page: response.number || 0, totalPages: response.totalPages || 0 }));
    } catch (error) {
      toast.error(`Không thể tải danh sách đề thi: ${error.message}`);
      setQuizzes([]);
    } finally {
      setLoading(false);
    }
  }, [debouncedSearchTerm, filters, pagination.page, pagination.size]); // Đã loại bỏ sortConfig

  useEffect(() => {
    loadQuizzes();
  }, [loadQuizzes]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
    setPagination(p => ({ ...p, page: 0 })); // Reset page on filter change
  };

  const handleEdit = (quizId) => {
    navigate(`/admin/management/edit/${quizId}`);
  };

  const handleDeleteRequest = (quiz) => {
    setQuizToDelete(quiz);
    setIsConfirmModalOpen(true);
  };

  const performDelete = async () => {
    if (!quizToDelete) return;
    try {
      await quizService.deleteSubmission(quizToDelete.id);
      toast.success('Đã xóa đề thi thành công!');
      // Tải lại danh sách sau khi xóa
      // Nếu xóa mục cuối cùng trên trang hiện tại (và đó không phải là trang đầu tiên), hãy lùi về trang trước
      if (quizzes.length === 1 && pagination.page > 0) {
        loadQuizzes(pagination.page - 1);
      } else {
        loadQuizzes(pagination.page);
      }
    } catch (error) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      setIsConfirmModalOpen(false);
      setQuizToDelete(null);
    }
  };

  const formatDate = (dateString) => new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  });

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h2 className="text-xl font-semibold mb-4">Quản lý Đề thi Đã Duyệt</h2>

      <div className="flex flex-wrap gap-4 mb-4 items-end">
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setPagination(p => ({ ...p, page: 0 }));
          }}
          placeholder="Tìm theo tiêu đề..."
          className="w-full sm:w-auto flex-grow p-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800"
        />
        <select
          name="subject"
          value={filters.subject}
          onChange={handleFilterChange}
          className="p-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800"
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
          name="difficulty"
          value={filters.difficulty}
          onChange={handleFilterChange}
          className="p-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800"
        >
          <option value="">Tất cả độ khó</option>
          <option value="EASY">Dễ</option>
          <option value="MEDIUM">Trung bình</option>
          <option value="HARD">Khó</option>
        </select>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full bg-white text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Tiêu đề</th>
              <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Môn học</th>
              <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Độ khó</th>
              <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Người đóng góp</th>
              <th className="py-3 px-4 border-b text-center font-medium text-gray-600">Số câu</th>
              <th className="py-3 px-4 border-b text-center font-medium text-gray-600">Thời gian</th>
              <th className="py-3 px-4 border-b text-left font-medium text-gray-600">Ngày tạo</th>
              <th className="py-3 px-4 border-b text-center font-medium text-gray-600 w-24">Hành động</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="7" className="text-center p-8 text-gray-500">Đang tải...</td></tr>
            ) : quizzes.length > 0 ? (
              quizzes.map(quiz => (
                <tr key={quiz.id} className="hover:bg-gray-50">
                  <td className="py-3 px-4 border-b font-medium text-gray-800">{quiz.title}</td>
                  <td className="py-3 px-4 border-b">
                    <span className="inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                      <BookOpen size={14} /> {subjectDisplayMap[quiz.subject] || quiz.subject}
                    </span>
                  </td>
                  <td className="py-3 px-4 border-b">
                    <span className={`inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium ${getDifficultyColor(quiz.difficultyLevel)}`}>
                      {difficultyDisplayMap[quiz.difficultyLevel]}
                    </span>
                  </td>
                  <td className="py-3 px-4 border-b text-gray-600">{quiz.contributor?.name || 'N/A'}</td>
                  <td className="py-3 px-4 border-b text-center text-gray-600">{quiz.questions?.length || 0}</td>
                  <td className="py-3 px-4 border-b text-center text-gray-600">
                    <span className="inline-flex items-center gap-1.5">{quiz.durationMinutes} phút</span>
                  </td>
                  <td className="py-3 px-4 border-b text-gray-600">{formatDate(quiz.createdAt)}</td>
                  <td className="py-3 px-4 border-b text-center">
                    <div className="flex justify-center gap-2">
                      <button onClick={() => handleEdit(quiz.id)} className="p-1.5 rounded-md text-blue-600 hover:bg-blue-100 hover:text-blue-700 transition" title="Sửa">
                        <Edit size={16} />
                      </button>
                      <button onClick={() => handleDeleteRequest(quiz)} className="p-1.5 rounded-md text-red-600 hover:bg-red-100 hover:text-red-700 transition" title="Xóa">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr><td colSpan="7" className="text-center p-8 text-gray-500">Không tìm thấy đề thi nào.</td></tr>
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
        isOpen={isConfirmModalOpen}
        onClose={() => setIsConfirmModalOpen(false)}
        onConfirm={performDelete}
        title="Xác nhận xóa"
        message={`Bạn có chắc chắn muốn xóa đề thi "${quizToDelete?.title}" không? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="danger"
      />
    </div>
  );
}