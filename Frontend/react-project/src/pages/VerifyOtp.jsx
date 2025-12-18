import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './VerifyOtp.css';

const API_BASE_URL = 'http://localhost:8081';

const VerifyOtp = () => {
  const [otp, setOtp] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!otp.trim()) {
      setError('Wpisz kod OTP.');
      return;
    }

    try {
      setLoading(true);
      const response = await fetch(`${API_BASE_URL}/user/verify-otp`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          otp,
        }),
      });

      if (!response.ok) {
        setError('Nieprawidłowy lub wygasły kod OTP.');
        return;
      }

      setMessage('Kod został poprawnie zweryfikowany. Możesz się teraz zalogować.');
      setTimeout(() => {
        navigate('/');
      }, 2000);
    } catch {
      setError('Brak połączenia z serwerem.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setError('');
    setMessage('');

    try {
      setLoading(true);
      const response = await fetch(`${API_BASE_URL}/user/send-otp`, {
        method: 'POST',
        credentials: 'include',
      });

      if (!response.ok) {
        setError('Nie udało się wysłać nowego kodu OTP.');
        return;
      }

      setMessage('Nowy kod OTP został wysłany na Twój email.');
    } catch {
      setError('Brak połączenia z serwerem podczas wysyłania kodu.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="verify-otp-page">
      <div className="verify-otp-card">
        <h2>Potwierdź swój email</h2>
        <p className="verify-otp-subtitle">
          Wysłaliśmy kod weryfikacyjny na adres powiązany z Twoim kontem.
          Wpisz go poniżej, aby dokończyć rejestrację.
        </p>

        <form onSubmit={handleSubmit} className="verify-otp-form">
          <label htmlFor="otp" className="verify-otp-label">Kod OTP</label>
          <input
            id="otp"
            type="text"
            maxLength={6}
            className="verify-otp-input"
            value={otp}
            onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, ''))}
            placeholder="Wpisz 6-cyfrowy kod"
          />

          {error && <div className="verify-otp-error">{error}</div>}
          {message && <div className="verify-otp-success">{message}</div>}

          <button type="submit" className="verify-otp-button" disabled={loading}>
            {loading ? 'Weryfikowanie...' : 'Potwierdź kod'}
          </button>
        </form>

        <button
          type="button"
          className="verify-otp-resend"
          onClick={handleResend}
          disabled={loading}
        >
          Wyślij kod ponownie
        </button>

        <button
          type="button"
          className="verify-otp-back"
          onClick={() => navigate('/')}
        >
          Wróć na stronę główną
        </button>
      </div>
    </div>
  );
};

export default VerifyOtp;
