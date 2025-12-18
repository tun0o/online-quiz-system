import React, { useState, useEffect, useRef } from 'react';
import { X, Check } from 'lucide-react';
import 'mathlive/static.css';

const MathEditorModal = ({ isOpen, onClose, onInsert, initialValue = '' }) => {
  const [value, setValue] = useState(initialValue);
  const mathfieldRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      // Import mathlive dynamically to avoid SSR issues
      import('mathlive').then(mathlive => {
        const mf = new mathlive.MathfieldElement({
          virtualKeyboardMode: 'manual', // 'onfocus' or 'manual'
        });
        mf.value = initialValue;
        mf.addEventListener('input', (evt) => {
          setValue(evt.target.value);
        });

        const container = mathfieldRef.current;
        if (container) {
          // Clear previous instance
          while (container.firstChild) {
            container.removeChild(container.firstChild);
          }
          container.appendChild(mf);
          mf.focus();
        }
      });
    }
  }, [isOpen, initialValue]);

  if (!isOpen) return null;

  const handleInsert = () => {
    onInsert(value);
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl shadow-xl">
        <h3 className="text-lg font-semibold mb-4">Soạn thảo công thức</h3>
        <div ref={mathfieldRef} className="math-field border border-gray-300 rounded-md p-2 text-lg"></div>
        <div className="flex justify-end gap-3 mt-6">
          <button onClick={onClose} className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50">Hủy</button>
          <button onClick={handleInsert} className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700">
            <Check size={16} /> Chèn công thức
          </button>
        </div>
      </div>
    </div>
  );
};

export default MathEditorModal;