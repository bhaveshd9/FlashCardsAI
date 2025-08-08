import React, { useState, useRef, useEffect } from 'react';
import { X, FileText, Link as LinkIcon, Upload, Loader2 } from 'lucide-react';
import { extractContent, processFileUpload } from '../services/contentService';

const AIContentModal = ({ isOpen, onClose, onContentExtracted }) => {
  const [activeTab, setActiveTab] = useState('text');
  const [content, setContent] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [fileName, setFileName] = useState('');
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      setContent('');
      setError('');
      setFileName('');
      setActiveTab('text');
    }
  }, [isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) {
      setError('Please enter some content');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      let extractedText = content;
      
      if (activeTab === 'file' && fileInputRef.current?.files?.[0]) {
        const file = fileInputRef.current.files[0];
        extractedText = await processFileUpload(file);
      } else if (activeTab === 'url') {
        const result = await extractContent(content, 'url');
        extractedText = result.extractedText;
      }
      
      onContentExtracted(extractedText);
      onClose();
    } catch (err) {
      console.error('Error processing content:', err);
      setError('Failed to process content. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      const validTypes = ['application/pdf', 'text/plain', 'text/markdown'];
      if (!validTypes.includes(file.type) && !file.name.match(/\.(pdf|txt|md)$/i)) {
        setError('Please upload a PDF, TXT, or Markdown file');
        return;
      }
      setFileName(file.name);
      setContent(file.name);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex justify-between items-center p-4 border-b">
          <h2 className="text-xl font-semibold">Add Content with AI</h2>
          <button 
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700"
            disabled={isLoading}
            type="button"
            aria-label="Close"
          >
            <X size={24} />
          </button>
        </div>
        
        {/* Tabs */}
        <div className="flex border-b">
          <button
            className={`flex-1 py-3 px-4 flex items-center justify-center gap-2 ${
              activeTab === 'text' ? 'border-b-2 border-indigo-600 text-indigo-600' : 'text-gray-600 hover:bg-gray-50'
            }`}
            onClick={() => setActiveTab('text')}
            disabled={isLoading}
            type="button"
          >
            <FileText size={18} />
            <span>Text</span>
          </button>
          <button
            className={`flex-1 py-3 px-4 flex items-center justify-center gap-2 ${
              activeTab === 'url' ? 'border-b-2 border-indigo-600 text-indigo-600' : 'text-gray-600 hover:bg-gray-50'
            }`}
            onClick={() => setActiveTab('url')}
            disabled={isLoading}
            type="button"
          >
            <LinkIcon size={18} />
            <span>URL</span>
          </button>
          <button
            className={`flex-1 py-3 px-4 flex items-center justify-center gap-2 ${
              activeTab === 'file' ? 'border-b-2 border-indigo-600 text-indigo-600' : 'text-gray-600 hover:bg-gray-50'
            }`}
            onClick={() => setActiveTab('file')}
            disabled={isLoading}
            type="button"
          >
            <Upload size={18} />
            <span>File</span>
          </button>
        </div>
        
        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {error && (
            <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-md text-sm">
              {error}
            </div>
          )}
          
          <form onSubmit={handleSubmit} className="space-y-4">
            {activeTab === 'file' ? (
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleFileChange}
                  className="hidden"
                  accept=".pdf,.txt,.md"
                  id="file-upload"
                />
                <label
                  htmlFor="file-upload"
                  className="cursor-pointer flex flex-col items-center justify-center space-y-2"
                >
                  <Upload className="h-12 w-12 text-gray-400" />
                  <p className="text-sm text-gray-600">
                    {fileName || 'Click to upload PDF, TXT, or MD file'}
                  </p>
                  <p className="text-xs text-gray-500">
                    {fileName ? 'Click to change file' : 'or drag and drop'}
                  </p>
                </label>
              </div>
            ) : (
              <div className="space-y-2">
                <label htmlFor="content" className="block text-sm font-medium text-gray-700">
                  {activeTab === 'url' ? 'Enter URL' : 'Enter text'}
                </label>
                <textarea
                  id="content"
                  rows={8}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder={activeTab === 'url' 
                    ? 'https://example.com/article'
                    : 'Paste your text here...'}
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  disabled={isLoading}
                />
              </div>
            )}
            
            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                disabled={isLoading}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 flex items-center"
                disabled={isLoading || !content.trim()}
              >
                {isLoading ? (
                  <>
                    <Loader2 className="animate-spin -ml-1 mr-2 h-4 w-4" />
                    Processing...
                  </>
                ) : 'Generate Flashcards'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AIContentModal;
