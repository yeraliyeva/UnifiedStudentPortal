import React, { createContext, useContext, useState, useEffect } from 'react';

const I18nContext = createContext();

export const I18nProvider = ({ children }) => {
    const [messages, setMessages] = useState({});
    const [language, setLanguage] = useState(localStorage.getItem('lang') || 'en');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchMessages = async () => {
            const cacheKey = `i18n_${language}`;
            const cached = localStorage.getItem(cacheKey);
            
            if (cached) {
                try {
                    const parsed = JSON.parse(cached);
                    // Cache for 24 hours (86400000 ms)
                    if (Date.now() - parsed.timestamp < 86400000) {
                        setMessages(parsed.data);
                        setLoading(false);
                        return; // Use cached data, no network request!
                    }
                } catch (e) {
                    console.warn("Invalid i18n cache", e);
                }
            }

            setLoading(true);
            try {
                const response = await fetch('http://localhost:8080/api/system/messages', {
                    headers: { 'Accept-Language': language }
                });
                if (response.ok) {
                    const data = await response.json();
                    setMessages(data);
                    localStorage.setItem(cacheKey, JSON.stringify({
                        timestamp: Date.now(),
                        data: data
                    }));
                } else {
                    console.error('Failed to fetch messages');
                }
            } catch (err) {
                console.error('Network error fetching messages:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchMessages();
    }, [language]);

    const changeLanguage = (newLang) => {
        localStorage.setItem('lang', newLang);
        setLanguage(newLang);
    };

    // Implements MessageFormat style formatting: "Hello {0}"
    const t = (key, ...args) => {
        let msg = messages[key];
        if (!msg) return key; // Fallback to key if not found
        
        args.forEach((arg, index) => {
            msg = msg.replace(new RegExp(`\\{${index}\\}`, 'g'), arg);
        });
        
        return msg;
    };

    return (
        <I18nContext.Provider value={{ t, changeLanguage, language, loading }}>
            {loading ? <div style={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh'}}>Loading language...</div> : children}
        </I18nContext.Provider>
    );
};

export const useI18n = () => useContext(I18nContext);
