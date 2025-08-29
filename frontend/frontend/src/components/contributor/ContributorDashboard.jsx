import { useState, useEffect } from 'react';
import { Plus, Clock, CheckCircle, XCircle, Eye, Edit, Trash2 } from 'lucide-react';
import { toast } from 'react-toastify';
import QuizSubmissionForm from './QuizSubmissionForm';
import ConfirmationModal from '../common/ConfirmationModal';
import { quizService } from '../../services/quizService';

export default function ContributorDashboard() {
  const [activeTab, setActiveTab] = useState('list'); // 'list', 'create', or 'edit'
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  });
  const [editingSubmission, setEditingSubmission] = useState(null);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [modalContent, setModalContent] = useState({});

  const loadSubmissions = async (page = 0) => {
    setLoading(true);
    try {
      const response = await quizService.getMySubmissions(1, page, 10);
      setSubmissions(response.content || []);
      setPagination({
        page: response.number || 0,
        size: response.size || 10,
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0
      });
    } catch (error) {
      console.error('Error loading submissions:', error);
      toast.error("Không thể tải danh sách đề thi.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'list') {
      loadSubmissions();
    } else if (activeTab === 'create') {
      setEditingSubmission(null); // Ensure form is clean for creation
    }
  }, [activeTab]);

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

    const labels = {
      PENDING: 'Chờ duyệt',
      APPROVED: 'Đã duyệt',
      REJECTED: 'Từ chối'
    };

    return (
      <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium border ${styles[status]}`}>
        {icons[status]}
        {labels[status]}
      </span>
    );
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleEdit = (submission) => {
    setEditingSubmission(submission);
    setActiveTab('edit');
  };

  const handleDeleteRequest = (id) => {
    setModalContent({
      title: 'Xác nhận xóa',
      message: 'Bạn có chắc chắn muốn xóa đề thi này không? Hành động này không thể hoàn tác.',
      confirmText: 'Xóa',
      variant: 'danger'
    });
    setConfirmAction(() => () => performDelete(id));
    setIsConfirmModalOpen(true);
  };

  const performDelete = async (id) => {
    try {
      await quizService.deleteSubmission(id);
      toast.success('Đã xóa đề thi thành công!');
      loadSubmissions();
    } catch (error) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      closeConfirmModal();
    }
  };

  const closeConfirmModal = () => {
    setIsConfirmModalOpen(false);
    setConfirmAction(null);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Đóng góp đề thi</h1>
        <div className="flex gap-2">
          <button
            onClick={() => setActiveTab('list')}
            className={`px-4 py-2 rounded-lg transition ${
              activeTab === 'list'
                ? 'bg-green-600 text-white'
                : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
            }`}
          >
            <Eye size={16} className="inline mr-2" />
            Đề của tôi
          </button>
          <button
            onClick={() => {
              // Luôn reset trạng thái sửa khi người dùng chủ động bấm "Tạo đề mới"
              setEditingSubmission(null);
              setActiveTab('create');
            }}
            className={`px-4 py-2 rounded-lg transition ${
              activeTab === 'create'
                ? 'bg-green-600 text-white'
                : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
            }`}
          >
            <Plus size={16} className="inline mr-2" />
            Tạo đề mới
          </button>
        </div>
      </div>

      {/* Content */}
      {activeTab === 'create' || activeTab === 'edit' ? (
        <QuizSubmissionForm 
          submission={editingSubmission}
          onSuccess={() => {
            setEditingSubmission(null);
            setActiveTab('list');
            loadSubmissions();
          }} 
        />
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          {/* Stats */}
          <div className="p-6 border-b border-gray-200">
            <div className="grid grid-cols-4 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-800">{pagination.totalElements}</div>
                <div className="text-sm text-gray-600">Tổng đề</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-yellow-600">
                  {submissions.filter(s => s.status === 'PENDING').length}
                </div>
                <div className="text-sm text-gray-600">Chờ duyệt</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">
                  {submissions.filter(s => s.status === 'APPROVED').length}
                </div>
                <div className="text-sm text-gray-600">Đã duyệt</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-red-600">
                  {submissions.filter(s => s.status === 'REJECTED').length}
                </div>
                <div className="text-sm text-gray-600">Từ chối</div>
              </div>
            </div>
          </div>

          {/* Submissions List */}
          <div className="p-6">
            {loading ? (
              <div className="text-center py-8">
                <div className="text-gray-500">Đang tải...</div>
              </div>
            ) : submissions.length === 0 ? (
              <div className="text-center py-8">
                <div className="text-gray-500 mb-4">Bạn chưa có đề thi nào</div>
                <button
                  onClick={() => setActiveTab('create')}
                  className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
                >
                  Tạo đề đầu tiên
                </button>
              </div>
            ) : (
              <div className="space-y-4">
                {submissions.map((submission) => (
                  <div key={submission.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition">
                    <div className="flex justify-between items-start mb-3">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-800 mb-1">{submission.title}</h3>
                        <p className="text-gray-600 text-sm mb-2">{submission.description}</p>
                        <div className="flex items-center gap-4 text-sm text-gray-500">
                          <span>Môn: {submission.subject}</span>
                          <span>Thời gian: {submission.durationMinutes}p</span>
                          <span>Câu hỏi: {submission.questions?.length || 0}</span>
                        </div>
                      </div>
                      <div className="text-right">
                        {getStatusBadge(submission.status)}
                        <div className="text-xs text-gray-500 mt-1">
                          {formatDate(submission.createdAt)}
                        </div>
                      </div>
                    </div>
                    
                    {submission.status === 'REJECTED' && submission.adminFeedback && (
                      <div className="bg-red-50 border border-red-200 rounded p-3 mt-3">
                        <div className="text-sm font-medium text-red-800 mb-1">Lý do từ chối:</div>
                        <div className="text-sm text-red-700">{submission.adminFeedback}</div>
                      </div>
                    )}

                    {submission.status === 'PENDING' && (
                      <div className="flex gap-2 mt-3">
                        <button
                          onClick={() => handleEdit(submission)}
                          className="flex items-center gap-1 px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 transition text-sm"
                        >
                          <Edit size={14} />
                          Sửa
                        </button>
                        <button
                          onClick={() => handleDeleteRequest(submission.id)}
                          className="flex items-center gap-1 px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 transition text-sm"
                        >
                          <Trash2 size={14} />
                          Xóa
                        </button>
                      </div>
                    )}
                  </div>
                ))}

                {/* Pagination */}
                {pagination.totalPages > 1 && (
                  <div className="flex justify-center items-center gap-2 mt-6">
                    {Array.from({ length: pagination.totalPages }, (_, i) => (
                      <button
                        key={i}
                        onClick={() => loadSubmissions(i)}
                        className={`px-3 py-1 rounded ${
                          i === pagination.page
                            ? 'bg-green-600 text-white'
                            : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        {i + 1}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      <ConfirmationModal
        isOpen={isConfirmModalOpen}
        onClose={closeConfirmModal}
        onConfirm={confirmAction}
        title={modalContent.title}
        message={modalContent.message}
        confirmText={modalContent.confirmText}
        variant={modalContent.variant}
        size={modalContent.size}
      />
    </div>
  );
}
