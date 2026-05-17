import React, { createContext, useContext, useState, useEffect } from 'react';

const I18nContext = createContext();

export const I18nProvider = ({ children }) => {
    const [messages, setMessages] = useState({});
    const [language, setLanguage] = useState(localStorage.getItem('lang') || 'en');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        Object.keys(localStorage)
            .filter(k => k.startsWith('i18n_'))
            .forEach(k => localStorage.removeItem(k));
    }, []);

    useEffect(() => {
        let cancelled = false;
        setLoading(true);
        fetch('http://localhost:8080/api/system/messages', {
            headers: { 'Accept-Language': language },
            cache: 'no-store',
        })
            .then(r => r.ok ? r.json() : Promise.reject(r.statusText))
            .then(data => { if (!cancelled) setMessages(data); })
            .catch(err => console.error('Failed to load translations:', err))
            .finally(() => { if (!cancelled) setLoading(false); });
        return () => { cancelled = true; };
    }, [language]);

    const changeLanguage = (newLang) => {
        localStorage.setItem('lang', newLang);
        setLanguage(newLang);
    };

    const t = (key, ...args) => {
        let msg = messages[key];
        if (!msg) return key;
        args.forEach((arg, index) => {
            msg = msg.replace(new RegExp(`\\{${index}\\}`, 'g'), arg);
        });
        return msg;
    };

    return (
        <I18nContext.Provider value={{ t, changeLanguage, language, loading }}>
            {loading ? <div style={{display:'flex',justifyContent:'center',alignItems:'center',height:'100vh'}}>Loading language…</div> : children}
        </I18nContext.Provider>
    );
};

export const useI18n = () => useContext(I18nContext);
