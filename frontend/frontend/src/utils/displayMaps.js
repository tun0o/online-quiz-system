// This file centralizes mappings for consistent display across the app.

export const subjectDisplayMap = {
  MATH: 'Toán học',
  PHYSICS: 'Vật lý',
  CHEMISTRY: 'Hóa học',
  BIOLOGY: 'Sinh học',
  LITERATURE: 'Ngữ văn',
  ENGLISH: 'Tiếng Anh',
};

export const difficultyDisplayMap = {
  EASY: 'Dễ',
  MEDIUM: 'Trung bình', 
  HARD: 'Khó'
};

export const getDifficultyColor = (difficulty) => {
  const colors = {
    EASY: 'text-green-600 bg-green-100',
    MEDIUM: 'text-yellow-600 bg-yellow-100',
    HARD: 'text-red-600 bg-red-100'
  };
  return colors[difficulty] || 'text-gray-600 bg-gray-100';
};

export const goalDisplayMap = {
  EXAM_PREP: 'Ôn thi',
  KNOWLEDGE_IMPROVEMENT: 'Nâng cao kiến thức',
  FUN: 'Học cho vui'
};
