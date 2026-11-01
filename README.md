# dpi for Android

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="dpi logo" style="border-radius: 28px;" />
</p>

<h3 align="center">Modern, Hafif ve Güçlü Android DPI Aşma & Sansür Engeli Kaldırma İstemcisi</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform" />
  <img src="https://img.shields.io/badge/Android%2015-16KB%20Page%20Ready-blue.svg" alt="16KB Compatible" />
  <img src="https://img.shields.io/badge/UI-Material%203%20Modern-indigo.svg" alt="Material 3" />
  <img src="https://img.shields.io/badge/Developer-patr10on-purple.svg" alt="Developer" />
  <img src="https://img.shields.io/badge/License-GPLv3-orange.svg" alt="License" />
</p>

---

## 🚀 Genel Bakış (Overview)

**dpi**, Android cihazlarda yerel bir VPN servisi ve SOCKS5 tüneli çalıştırarak Deep Packet Inspection (DPI) filtrelerini, SNI engellerini ve erişim kısıtlamalarını aşmanızı sağlayan açık kaynaklı bir araçtır.

Trafik uzaktaki bir sunucuya gönderilmez; tamamen cihazınızın içinde yerel paket desenkronizasyonu (split, disorder, fake sni, oob) uygulanarak doğrudan hedefe ulaştırılır. **Root gerektirmez.**

---

## ✨ Yeni ve Öne Çıkan Özellikler (Features)

* 🎨 **Yenilenmiş Material 3 Arayüz:** Modern neon vurgular, dinamik parlayan bağlantı butonu, durum kartları ve akıcı animasyonlar.
* ⚡ **Android 15 (16 KB Page Size) Tam Uyumluluğu:** 64-bit modern Android cihazlar için `-Wl,-z,max-page-size=16384` ile ELF ve ZIP hizalaması tamamlanmış yerel C motoru.
* 📊 **Canlı Ağ Trafiği & Hız Göstergesi:** Anlık indirme/yükleme (Down/Up) hızları ve toplam aktarılan veri sayaçları.
* 🎯 **Ön Tanımlı Profiller (Dpi Presets):**
  * **Dengeli (Varsayılan):** Standart paket bölme ve OOB modu (`-s1 -q1 -Y`)
  * **Discord & Ses İletişimi:** UDP sahte paket koruması (`-s1 -q1 -a1`)
  * **Katı Filtreleme / Superonline:** SNI sahteleme ve TLS kayıt bölme (`-s1 -d1 -f -1 -r 1+s`)
  * **Türk Telekom / Hızlı:** Düşük gecikmeli bant dışı desenkronizasyon (`-s1 -o1 -Y`)
  * **Hafif (Sadece Split):** Temel DPI aşma yöntemi (`-s1`)
  * **Özel (Custom):** Tam komut satırı serbestliği.
* 🐕 **Otomatik Yeniden Bağlanma (Watchdog):** Ağ kopması veya proxy kesintisinde otomatik devreye giren koruma.
* 🔀 **Uygulama Filtreleme (Bölünmüş Tünel / Split Tunneling):** Seçilen uygulamaları tünel dışına çıkarma desteği.
* ✈️ **Telegram Entegrasyonu:** Bağlantı kurulduğunda Telegram botu üzerinden anlık durum bildirimi gönderme.
* 🛠️ **Geliştirici Sayfası:** Doğrudan arayüz üzerinden profil bağlantıları ve sosyal medya entegrasyonu.

---

## 🛠️ Derleme ve Kurulum (Build Instructions)

Projeyi Android Studio veya terminal üzerinden derlemek için:

### Gereksinimler
* JDK 17+
* Android SDK (API 34)
* Android NDK (r26 veya üzeri, 16KB bellek sayfası desteği için)
* CMake (3.22.1+)

### Terminal Üzerinden Derleme
```bash
# Depoyu klonlayın
git clone https://github.com/Patr10on/dpi-android.git
cd dpi-android

# Debug APK derleyin
./gradlew assembleDebug

# Çıktı dosyası:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Sıkça Sorulan Sorular (FAQ)

### Bu bir VPN midir?
Hayır. Uygulama trafiği yerel olarak `hev-socks5-tunnel` üzerinden yönlendirmek için Android'in `VpnService` altyapısını kullanır; ancak harici bir VPN sunucusuna bağlanmaz. Trafiğiniz şifrelenip başka bir ülkeye aktarılmaz, IP adresinizi gizlemez.

### Root yetkisi gerekir mi?
Hayır. Tüm işlemler kullanıcı alanında (User-Space) yerel TUN arabirimiyle yürütülür.

### Hangi sağlayıcılarda çalışır?
Superonline, Türk Telekom, Vodafone, TurkNet ve diğer yerel ISP'lerin uyguladığı DPI kısıtlamalarını aşmak için yukarıdaki hazır profiller test edilip optimize edilmiştir.

---

## 👨‍💻 Geliştirici (Developer)

* **Geliştirici:** Bayram ([@Patr10on](https://github.com/Patr10on))
* **Instagram:** [@sgepv](https://instagram.com/sgepv)
* **GitHub:** [https://github.com/Patr10on](https://github.com/Patr10on)

---

## 📄 Lisans (License)

Bu proje [GPL-3.0 Lisansı](LICENSE) altında lisanslanmıştır. Orijinal motor [ByeDPI (hufrea)](https://github.com/hufrea/byedpi) ve [hev-socks5-tunnel (heiher)](https://github.com/heiher/hev-socks5-tunnel) projelerini temel alır.
