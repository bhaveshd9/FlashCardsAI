import axios from 'axios';
import { getAuthToken } from './authService';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

/**
 * Extract text content from various sources (text, URL, PDF)
 * @param {string} content - The content to process (text content, URL, or base64-encoded PDF)
 * @param {string} contentType - The type of content: 'text', 'url', or 'pdf'
 * @param {Object} options - Additional options
 * @param {string} [options.mimeType] - MIME type for PDF uploads
 * @param {string} [options.fileName] - Original filename for PDFs
 * @returns {Promise<Object>} - The extracted content and metadata
 */
export const extractContent = async (content, contentType, options = {}) => {
  try {
    const token = getAuthToken();
    const response = await axios.post(
      `${API_BASE_URL}/content/extract`,
      {
        content,
        contentType,
        mimeType: options.mimeType,
        fileName: options.fileName
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      }
    );
    
    return {
      success: true,
      data: response.data
    };
  } catch (error) {
    console.error('Error extracting content:', error);
    return {
      success: false,
      error: error.response?.data?.error || 'Failed to extract content'
    };
  }
};

/**
 * Process a file upload and extract text content
 * @param {File} file - The file to process
 * @returns {Promise<Object>} - The extracted content and metadata
 */
export const processFileUpload = async (file) => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    
    reader.onload = async (event) => {
      try {
        // For PDF files, we'll send the base64 encoded content
        const base64Content = event.target.result.split(',')[1];
        const result = await extractContent(
          base64Content,
          'pdf',
          {
            mimeType: file.type,
            fileName: file.name
          }
        );
        resolve(result);
      } catch (error) {
        console.error('Error processing file:', error);
        resolve({
          success: false,
          error: 'Failed to process file'
        });
      }
    };
    
    reader.onerror = () => {
      resolve({
        success: false,
        error: 'Error reading file'
      });
    };
    
    if (file.type === 'application/pdf') {
      reader.readAsDataURL(file);
    } else {
      reader.readAsText(file);
    }
  });
};

export default {
  extractContent,
  processFileUpload
};
