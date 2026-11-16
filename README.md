# 💬 Smart Messages

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Release](https://img.shields.io/github/v/release/shahin0964/Smart-Messages?style=for-the-badge&color=2563EB)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)

**A modern, ultra-fast, intelligent Default SMS & Messaging application for Android powered by Jetpack Compose & Material 3.**

[English](#-features) • [বাংলা বিবরণ](#-বাংলা-বিবরণ) • [Download APK](#-download--installation)

</div>

---

## 🌟 Key Features

### 🔑 1. One-Tap OTP Copy
Never manually memorize or switch apps to copy an OTP again! Smart Messages detects 4 to 8 digit verification codes from banks, apps, and services:
* **Interactive Notification Action:** Copy the OTP directly from your heads-up notification without opening the app.
* **In-Chat Quick Copy:** A dedicated, highlighted chip in both inbox threads and chat bubbles lets you copy the code in one tap.

### 📂 2. Smart Categorization Tabs
Keeps your inbox clean, clutter-free, and well organized:
* **All:** Complete overview of all conversations.
* **Personal:** Genuine conversations with friends, family, and personal contacts.
* **OTPs:** Automatic separation of verification codes (Google, WhatsApp, bKash, banks, etc.).
* **Transactions:** Financial updates, debits, credits, and invoices (bKash, Nagad, Rocket, Upay, Banks, Cards).
* **Spam:** Automated spam and promotional offer filtering.

### 🛡️ 3. Scam & Spam Protection
* Highlights suspicious links and phishing attempts.
* Blocks unwanted spam senders.

### 🎨 4. Modern Material 3 UI & Dark Mode
* Beautiful bubble chat interface with adaptive colors.
* Edge-to-edge support with dynamic theming.
* Dual-SIM badge and fast search across contacts and messages.

### 📞 5. Seamless Dialer Integration
* Directly launch phone calls to any contact straight from the chat header.

---

## 🇧🇩 বাংলা বিবরণ

**Smart Messages** হলো অ্যান্ড্রয়েডের জন্য একটি অত্যন্ত দ্রুতগতির ও আধুনিক ডিফল্ট এসএমএস অ্যাপ, যা তৈরি করা হয়েছে Jetpack Compose এবং Material 3 দিয়ে।

### প্রধান সুবিধাসমূহ:
1. **এক ক্লিকে OTP কপি:** নোটিফিকেশন বার কিংবা মেসেজের ভেতর থেকে কোড টাইপ না করেই ১-ট্যাপে ওটিপি কপি করার সুবিধা।
2. **স্মার্ট ক্যাটাগরি ফিল্টার:** 
   - **Personal:** বন্ধুদের সাথে ব্যক্তিগত মেসেজ।
   - **OTPs:** ব্যাংক ও সোশ্যাল মিডিয়ার ভেরিফিকেশন কোড।
   - **Transactions:** বিকাশ, নগদ, রকেট এবং ব্যাংকের লেনদেনের হিসাব আলাদা ট্যাবে।
   - **Spam:** বিজ্ঞাপন ও স্প্যাম মেসেজ স্বয়ংক্রিয়ভাবে ফিল্টার।
3. **১০০% প্রাইভেসি-ফার্স্ট:** আপনার কোনো মেসেজ বা তথ্য কোনো সার্ভারে পাঠানো হয় না; সবকিছু আপনার ডিভাইসের ভেতরেই নিরাপদে প্রসেস হয়।
4. **চ্যাট থেকে সরাসরি কল:** মেসেজ পড়তে পড়তেই উপরের কল বাটনে চাপ দিয়ে যেকোনো ডায়ালার (বা Smart Dialer) দিয়ে কল করার সুবিধা।

---

## 📥 Download & Installation

You can always download the latest compiled APK directly from the GitHub Releases:

👉 **[Download Latest SmartMessages APK](https://github.com/shahin0964/Smart-Messages/releases/latest)**

1. Download the `SmartMessages-latest.apk`.
2. Tap the downloaded file to install on your Android device (Android 8.0+ / API 26+ supported).
3. Open the app and grant required SMS permissions, then set it as your **Default SMS App** to unlock all smart features.

---

## 🛠️ Architecture & Tech Stack

* **Language:** Kotlin 2.0
* **UI Toolkit:** Jetpack Compose + Material 3 Design
* **Architecture:** MVVM + Coroutines + StateFlow
* **Telephony Stack:**
  * `SmsReceiver` (`SMS_DELIVER_ACTION`, `SMS_RECEIVED_ACTION`)
  * `MmsReceiver` (`WAP_PUSH_DELIVER_ACTION`)
  * `HeadlessSmsSendService` (`RESPOND_VIA_MESSAGE`)
* **CI/CD:** Automated builds and GitHub Releases via GitHub Actions

---

## 📄 License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.