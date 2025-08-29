import { createContext, useContext, useState } from 'react';

const QuizContext = createContext();

export function QuizProvider({ children }) {
  const [submissions, setSubmissions] = useState([]);
  const [pendingSubmissions, setPendingSubmissions] = useState([]);
  const [loading, setLoading] = useState(false);

  // Mock user cho development
  const [currentUser, setCurrentUser] = useState({
    id: 1,
    name: "Test User",
    role: "CONTRIBUTOR" // CONTRIBUTOR hoặc ADMIN
  });

  return (
    <QuizContext.Provider value={{
      submissions,
      setSubmissions,
      pendingSubmissions,
      setPendingSubmissions,
      loading,
      setLoading,
      currentUser,
      setCurrentUser
    }}>
      {children}
    </QuizContext.Provider>
  );
}

export const useQuiz = () => {
  const context = useContext(QuizContext);
  if (!context) {
    throw new Error('useQuiz must be used within QuizProvider');
  }
  return context;
};