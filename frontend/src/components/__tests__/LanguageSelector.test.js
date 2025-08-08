import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../i18n/i18n';
import LanguageSelector from '../LanguageSelector';

// Mock i18n.changeLanguage
i18n.changeLanguage = jest.fn((lng) => {
  i18n.language = lng;
  return Promise.resolve();
});

describe('LanguageSelector', () => {
  // Mock i18n event listeners
  const eventListeners = {};
  const mockOn = jest.fn((event, callback) => {
    eventListeners[event] = callback;
    return () => delete eventListeners[event];
  });
  
  const mockOff = jest.fn((event) => {
    delete eventListeners[event];
  });
  
  // Mock i18n
  const originalI18n = { ...i18n };
  
  beforeEach(() => {
    // Reset i18n mock
    i18n.language = 'en';
    i18n.on = mockOn;
    i18n.off = mockOff;
    
    // Clear all mocks before each test
    jest.clearAllMocks();
    
    // Mock localStorage
    Storage.prototype.setItem = jest.fn();
    
    // Reset event listeners
    Object.keys(eventListeners).forEach(key => delete eventListeners[key]);
  });
  
  afterAll(() => {
    // Restore original i18n
    Object.assign(i18n, originalI18n);
  });

  const renderComponent = () => {
    return render(
      <I18nextProvider i18n={i18n}>
        <LanguageSelector />
      </I18nextProvider>
    );
  };

  it('renders with default language', () => {
    renderComponent();
    
    // Should show the globe icon and default language flag (US flag for English)
    const button = screen.getByRole('button', { name: /select language/i });
    expect(button).toBeInTheDocument();
    expect(button).toHaveTextContent('🇺🇸');
  });

  it('opens and closes dropdown when clicked', () => {
    renderComponent();
    
    // Initially dropdown should not be visible
    expect(screen.queryByRole('menu')).not.toBeInTheDocument();
    
    // Click to open dropdown
    const button = screen.getByRole('button', { name: /select language/i });
    fireEvent.click(button);
    
    // Dropdown should be visible with language options
    const dropdown = screen.getByRole('menu');
    expect(dropdown).toBeInTheDocument();
    expect(screen.getByText('English')).toBeInTheDocument();
    expect(screen.getByText('Español')).toBeInTheDocument();
    expect(screen.getByText('हिंदी')).toBeInTheDocument();
    
    // Click again to close
    fireEvent.click(button);
    expect(screen.queryByRole('menu')).not.toBeInTheDocument();
  });

  it('changes language when a language is selected', async () => {
    renderComponent();
    
    // Open the dropdown
    const button = screen.getByRole('button', { name: /select language/i });
    fireEvent.click(button);
    
    // Click on Spanish
    const spanishButton = screen.getByText('Español');
    await act(async () => {
      fireEvent.click(spanishButton);
    });
    
    // Should call changeLanguage with 'es'
    expect(i18n.changeLanguage).toHaveBeenCalledWith('es');
    
    // Should update localStorage
    expect(localStorage.setItem).toHaveBeenCalledWith('i18nextLng', 'es');
    
    // Should close the dropdown
    expect(screen.queryByRole('menu')).not.toBeInTheDocument();
  });

  it('updates when language changes externally', () => {
    const { rerender } = renderComponent();
    
    // Simulate external language change
    act(() => {
      i18n.language = 'es';
      // Trigger the languageChanged event
      if (eventListeners.languageChanged) {
        eventListeners.languageChanged('es');
      }
    });
    
    // Rerender to see the updated language
    rerender(
      <I18nextProvider i18n={i18n}>
        <LanguageSelector />
      </I18nextProvider>
    );
    
    // Should show the Spanish flag
    const button = screen.getByRole('button', { name: /select language/i });
    expect(button).toHaveTextContent('🇪🇸');
  });

  it('closes dropdown when clicking outside', () => {
    // Mock document.addEventListener
    const addEventListenerSpy = jest.spyOn(document, 'addEventListener');
    
    // Render the component
    const { container } = renderComponent();
    
    // Open the dropdown
    const button = screen.getByRole('button', { name: /select language/i });
    fireEvent.click(button);
    
    // Simulate clicking outside
    fireEvent.mouseDown(document.body);
    
    // Dropdown should be closed
    expect(screen.queryByRole('menu')).not.toBeInTheDocument();
    
    // Cleanup
    addEventListenerSpy.mockRestore();
  });
  
  it('cleans up event listeners on unmount', () => {
    const removeEventListenerSpy = jest.spyOn(document, 'removeEventListener');
    const { unmount } = renderComponent();
    
    // Unmount the component
    unmount();
    
    // Should have removed the mousedown event listener
    expect(removeEventListenerSpy).toHaveBeenCalledWith('mousedown', expect.any(Function));
    
    // Should have removed the i18n event listener
    expect(mockOff).toHaveBeenCalledWith('languageChanged', expect.any(Function));
    
    // Cleanup
    removeEventListenerSpy.mockRestore();
  });
});
