# Gmail App Password Configuration Guide

## 🔍 **Current Issue**
The forgot password functionality is failing because of Gmail authentication issues. The error "Unable to process your request at this time" occurs when the email service cannot authenticate with Gmail.

## 🛠️ **Solution Options**

### **Option 1: Generate New Gmail App Password (Recommended)**

#### Step 1: Enable 2-Factor Authentication
1. Go to [Google Account Settings](https://myaccount.google.com/)
2. Navigate to "Security"
3. Enable "2-Step Verification" if not already enabled

#### Step 2: Generate App Password
1. In Google Account Settings → Security
2. Find "App passwords" (under 2-Step Verification)
3. Click "App passwords"
4. Select "Mail" as the app
5. Select "Other (Custom name)" as device
6. Enter "EShopProject" as the name
7. Click "Generate"
8. Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

#### Step 3: Update Configuration
Update `src/main/resources/application.properties`:

```properties
spring.mail.password=YOUR_NEW_APP_PASSWORD
```

Replace `YOUR_NEW_APP_PASSWORD` with the generated password (remove spaces).

### **Option 2: Use Gmail SMTP with OAuth2 (Advanced)**

If you prefer OAuth2 authentication, you can configure it in `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-oauth2-token
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.auth.mechanisms=XOAUTH2
```

### **Option 3: Test Without Email (Temporary)**

The application now includes a fallback mechanism. When email sending fails:

1. Check the console output for a reset link
2. Copy the link and test the password reset functionality
3. The link will be displayed in this format:
   ```
   === EMAIL SENDING FAILED - RESET LINK FOR TESTING ===
   Reset link: http://localhost:8080/reset-password?token=abc123...
   ```

## 🔧 **Testing Steps**

### **Step 1: Test Current Configuration**
1. Start the application
2. Go to `/forgot-password`
3. Enter a valid email address
4. Check console output for detailed error messages

### **Step 2: Check Error Messages**
The enhanced logging will show specific error types:
- `535-5.7.8`: Authentication failed
- `535-5.7.9`: App password required
- `Authentication`: General authentication error

### **Step 3: Verify Fix**
After updating the app password:
1. Restart the application
2. Test forgot password again
3. Check if email is received
4. Verify reset link works

## 📧 **Email Configuration Details**

### **Current Configuration**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=vikramvishu2@gmail.com
spring.mail.password=nqtt zwyd wcaf ymod
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### **Common Issues**
1. **App password expired**: Generate a new one
2. **2FA not enabled**: Enable 2-Step Verification first
3. **Wrong password format**: Remove spaces from app password
4. **Account security**: Check if Google blocked the login

## 🚀 **Quick Fix Steps**

1. **Generate new app password** (Option 1 above)
2. **Update `application.properties`** with new password
3. **Restart application**
4. **Test forgot password functionality**

## 📝 **Debug Information**

The application now provides detailed logging:
- Email service debug messages
- Specific Gmail error codes
- Fallback reset links for testing
- User existence verification

## ⚠️ **Security Notes**

- Never commit app passwords to version control
- Use environment variables for production
- Regularly rotate app passwords
- Monitor email sending logs

## 🆘 **If Still Having Issues**

1. Check Gmail account security settings
2. Verify 2-Step Verification is enabled
3. Try generating a new app password
4. Check if Gmail account has any restrictions
5. Test with a different Gmail account

---

**Note**: The application now includes fallback functionality that will show reset links in the console when email sending fails, allowing you to test the password reset functionality even without working email configuration. 