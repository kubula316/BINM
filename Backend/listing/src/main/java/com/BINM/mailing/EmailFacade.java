package com.BINM.mailing;

public interface EmailFacade {

    void sendWelcomeEmail(String toEmail, String name);
    void sendResetOtpEmail(String toEmail, String otp);
    void sendOtpEmail(String toEmail, String otp);

}
