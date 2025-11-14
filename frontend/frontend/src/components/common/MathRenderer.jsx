import React from 'react';
import { InlineMath, BlockMath } from 'react-katex';

/**
 * Component này sẽ phân tích một chuỗi văn bản, tìm các công thức toán học
 * được viết bằng cú pháp LaTeX và hiển thị chúng.
 * - Inline math: \( ... \)
 * - Block math: $$ ... $$
 */
const MathRenderer = ({ text }) => {
  if (!text) {
    return null;
  }

  // Regex để tìm các biểu thức toán học inline và block
  const mathRegex = /(\\\(.*?\\\)|(?:\$\$[\s\S]*?\$\$))/g;
  const parts = text.split(mathRegex);

  return (
    <span className="whitespace-pre-wrap">
      {parts.map((part, index) => {
        if (part.startsWith('\\(') && part.endsWith('\\)')) {
          return <InlineMath key={index} math={part.substring(2, part.length - 2)} />;
        }
        if (part.startsWith('$$') && part.endsWith('$$')) {
          return <BlockMath key={index} math={part.substring(2, part.length - 2)} />;
        }
        return <span key={index}>{part}</span>;
      })}
    </span>
  );
};

export default MathRenderer;