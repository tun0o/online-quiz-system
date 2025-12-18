import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

/**
 * Component phân trang dùng chung.
 * @param {object} props
 * @param {number} props.currentPage - Trang hiện tại (bắt đầu từ 0).
 * @param {number} props.totalPages - Tổng số trang.
 * @param {function} props.onPageChange - Hàm callback khi chuyển trang, nhận vào số trang mới.
 * @param {number} [props.pageRange=2] - Số lượng trang hiển thị xung quanh trang hiện tại.
 */
const Pagination = ({ currentPage, totalPages, onPageChange, pageRange = 2 }) => {
    if (totalPages <= 1) {
        return null; // Không hiển thị phân trang nếu chỉ có 1 trang hoặc ít hơn
    }

    const getPageNumbers = () => {
        const pages = [];
        const startPage = Math.max(0, currentPage - pageRange);
        const endPage = Math.min(totalPages - 1, currentPage + pageRange);

        // Luôn hiển thị trang đầu tiên
        if (startPage > 0) {
            pages.push(0);
            if (startPage > 1) {
                pages.push('...');
            }
        }

        // Hiển thị các trang xung quanh trang hiện tại
        for (let i = startPage; i <= endPage; i++) {
            pages.push(i);
        }

        // Luôn hiển thị trang cuối cùng
        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) {
                pages.push('...');
            }
            pages.push(totalPages - 1);
        }

        return pages;
    };

    const pageNumbersToDisplay = getPageNumbers();

    return (
        <div className="flex justify-center items-center gap-2 mt-6">
            <button
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-3 py-1 rounded bg-white border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
            >
                <ChevronLeft size={16} /> Trước
            </button>

            {pageNumbersToDisplay.map((page, index) => (
                page === '...' ? (
                    <span key={index} className="px-3 py-1 text-gray-500">...</span>
                ) : (
                    <button
                        key={page}
                        onClick={() => onPageChange(page)}
                        className={`px-3 py-1 rounded transition ${page === currentPage
                            ? 'bg-green-600 text-white'
                            : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                        }`}
                    >
                        {page + 1}
                    </button>
                )
            ))}

            <button
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className="px-3 py-1 rounded bg-white border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
            >
                Sau <ChevronRight size={16} />
            </button>
        </div>
    );
};

export default Pagination;
