# 🎓 CHƯƠNG 3: FREQUENCY-DOMAIN REPRESENTATION OF SIGNALS

---

## **1️⃣ Ý tưởng (Basic Idea)**

- Mọi tín hiệu rời rạc đều có thể được biểu diễn như tổng (hoặc tích phân) của **các sóng hình sin hoặc mũ phức**.  
- Dạng cơ bản:
  
$$
x[n] = A \cos(2\pi f_0 n + \theta)
$$

- Chu kỳ xảy ra khi:

$$
f_0 = \frac{k}{N}
$$

- Tần số góc cơ bản:

$$
\omega_0 = \frac{2\pi}{N}
$$

⟹ Mỗi tín hiệu tuần hoàn có thể biểu diễn bằng **tập hợp hữu hạn các thành phần tần số rời rạc**.

---

## **2️⃣ Chuỗi Fourier thời gian rời rạc (DTFS – Discrete-Time Fourier Series)**

Áp dụng cho **tín hiệu rời rạc tuần hoàn** có chu kỳ \( N \).

---

### **(a) Phân tích (Analysis Equation)**

$$
c_k = \frac{1}{N} \sum_{n=0}^{N-1} x[n] e^{-j k \omega_0 n}
$$

### **(b) Tổng hợp (Synthesis Equation)**

$$
x[n] = \sum_{k=0}^{N-1} c_k e^{j k \omega_0 n}
$$

### **(c) Tham số**

$$
\omega_0 = \frac{2\pi}{N}
$$

Trong đó:
- \( c_k \): hệ số Fourier (Fourier coefficients)
- \( N \): chu kỳ cơ bản
- \( \omega_0 \): tần số góc cơ bản

---

### **(d) Tính chất phổ (Spectral Properties)**

| Tính chất | Biểu thức | Giải thích |
|------------|------------|------------|
| Chu kỳ phổ | \( c_{k+N} = c_k \) | Phổ lặp lại theo chu kỳ N |
| Đối xứng phức (x[n] thực) | \( c_{-k} = c_k^* \) | Phổ có tính đối xứng phức |
| Công suất trung bình (Parseval) | \( P = \frac{1}{N}\sum_{n=0}^{N-1}|x[n]|^2 = \sum_{k=0}^{N-1}|c_k|^2 \) | Bảo toàn năng lượng |

---

## **3️⃣ Định lý Parseval (Bảo toàn năng lượng)**

$$
\sum_{n=0}^{N-1} |x[n]|^2 = N \sum_{k=0}^{N-1} |c_k|^2
$$

Hoặc dạng công suất trung bình:

$$
P_x = \frac{1}{N}\sum_{n=0}^{N-1}|x[n]|^2 = \sum_{k=0}^{N-1}|c_k|^2
$$

---

## **4️⃣ Biến đổi Fourier thời gian rời rạc (DTFT)**

Áp dụng cho **tín hiệu rời rạc không tuần hoàn (aperiodic)**.

---

### **(a) Định nghĩa (DTFT Definition)**

$$
X(e^{j\omega}) = \sum_{n=-\infty}^{\infty} x[n] e^{-j\omega n}
$$

---

### **(b) Biến đổi ngược (Inverse DTFT)**

$$
x[n] = \frac{1}{2\pi} \int_{-\pi}^{\pi} X(e^{j\omega}) e^{j\omega n} d\omega
$$

---

### **(c) Tính chất quan trọng của DTFT**

| Tính chất | Biểu thức | Giải thích |
|------------|------------|------------|
| Tuyến tính | \( a_1x_1[n]+a_2x_2[n] \leftrightarrow a_1X_1(e^{j\omega})+a_2X_2(e^{j\omega}) \) | Tổng trong thời gian ↔ tổng trong tần số |
| Dịch thời gian | \( x[n-n_0] \leftrightarrow e^{-j\omega n_0} X(e^{j\omega}) \) | Dịch trong thời gian ↔ nhân pha trong tần số |
| Nhân với \( e^{j\omega_0 n} \) | \( e^{j\omega_0 n}x[n] \leftrightarrow X(e^{j(\omega-\omega_0)}) \) | Dịch phổ |
| Gấp thời gian | \( x[-n] \leftrightarrow X(e^{-j\omega}) \) | Đảo trục thời gian ↔ đảo trục tần số |
| Phức liên hợp | \( x^*[n] \leftrightarrow X^*(e^{-j\omega}) \) | Liên hợp ↔ đối xứng phức |
| Tích chập | \( x_1[n]*x_2[n] \leftrightarrow X_1(e^{j\omega})X_2(e^{j\omega}) \) | Chập thời gian ↔ nhân phổ |
| Nhân tín hiệu | \( x_1[n]x_2[n] \leftrightarrow \frac{1}{2\pi}(X_1 * X_2)(e^{j\omega}) \) | Nhân thời gian ↔ chập tần số |

---

## **5️⃣ So sánh các loại biểu diễn Fourier**

| Loại tín hiệu | Biểu diễn Fourier | Tên đầy đủ | Ghi chú |
|----------------|------------------|-------------|----------|
| CT tuần hoàn | Chuỗi Fourier (CTFS) | Continuous-Time Fourier Series | Liên tục & tuần hoàn |
| CT không tuần hoàn | Biến đổi Fourier (CTFT) | Continuous-Time Fourier Transform | Liên tục & vô hạn |
| DT tuần hoàn | Chuỗi Fourier rời rạc (DTFS) | Discrete-Time Fourier Series | Rời rạc & tuần hoàn |
| DT không tuần hoàn | Biến đổi Fourier rời rạc theo thời gian (DTFT) | Discrete-Time Fourier Transform | Rời rạc & vô hạn |

---

## **6️⃣ Ví dụ minh họa**

### **Ví dụ 1:**  
Nếu \( x[n] = 1 \) cho \( 0 \le n \le N-1 \):

$$
c_k = \frac{1}{N} \frac{1 - e^{-j k 2\pi}}{1 - e^{-j k \frac{2\pi}{N}}}
$$

---

### **Ví dụ 2:**  
Nếu \( x[n] = \cos(\omega_0 n) \):

$$
X(e^{j\omega}) = \pi[\delta(\omega - \omega_0) + \delta(\omega + \omega_0)]
$$

---

## **7️⃣ Ứng dụng của miền tần số**

- Phân tích phổ của tín hiệu tuần hoàn.  
- Xác định các thành phần tần số mạnh (dominant frequency).  
- Thiết kế và phân tích **bộ lọc số (Digital Filters)**.  
- Nén dữ liệu (MP3, JPEG) dựa vào phổ.  
- Phân tích âm thanh, giọng nói, radar, EEG, v.v.

---

## **8️⃣ Bảng tổng hợp công thức Fourier rời rạc**

| Nội dung | Biểu thức |
|-----------|------------|
| **Tín hiệu cơ bản** | \( x[n] = A\cos(2\pi f_0 n + \theta) \) |
| **Chu kỳ tuần hoàn** | \( f_0 = \frac{k}{N} \) |
| **Tần số góc cơ bản** | \( \omega_0 = \frac{2\pi}{N} \) |
| **DTFS phân tích** | \( c_k = \frac{1}{N}\sum_{n=0}^{N-1}x[n]e^{-jk\omega_0n} \) |
| **DTFS tổng hợp** | \( x[n] = \sum_{k=0}^{N-1}c_k e^{jk\omega_0n} \) |
| **DTFT** | \( X(e^{j\omega}) = \sum_{n=-\infty}^{\infty}x[n]e^{-j\omega n} \) |
| **DTFT ngược** | \( x[n] = \frac{1}{2\pi}\int_{-\pi}^{\pi}X(e^{j\omega})e^{j\omega n}d\omega \) |
| **Parseval** | \( \sum|x[n]|^2 = N\sum|c_k|^2 \) |
| **Tích chập** | \( x_1[n]*x_2[n] \leftrightarrow X_1(e^{j\omega})X_2(e^{j\omega}) \) |

---

## **9️⃣ Ghi nhớ nhanh – Flash Notes**

- DTFS dùng cho **tín hiệu tuần hoàn**.  
- DTFT dùng cho **tín hiệu vô hạn, không tuần hoàn**.  
- \( y[n] = x[n]*h[n] \leftrightarrow Y(e^{j\omega}) = X(e^{j\omega})H(e^{j\omega}) \).  
- Dịch thời gian ↔ nhân pha \( e^{-j\omega n_0} \).  
- Năng lượng bảo toàn (Parseval).  
- Phổ rời rạc \( c_k \) lặp lại theo chu kỳ N.

---

📘 **Công thức bắt buộc phải nhớ để thi:**

1. \( c_k = \frac{1}{N}\sum x[n]e^{-jk\omega_0n} \)  
2. \( x[n] = \sum c_k e^{jk\omega_0n} \)  
3. \( X(e^{j\omega}) = \sum x[n]e^{-j\omega n} \)  
4. \( x[n] = \frac{1}{2\pi}\int X(e^{j\omega})e^{j\omega n}d\omega \)  
5. \( y[n] = x[n]*h[n] \leftrightarrow Y(e^{j\omega})=X(e^{j\omega})H(e^{j\omega}) \)

---

✅ **Tóm lại:**
- DTFS ⇔ phân tích phổ tín hiệu **tuần hoàn**  
- DTFT ⇔ phân tích phổ tín hiệu **vô hạn / không tuần hoàn**  
- Cả hai biểu diễn **nội dung tần số của tín hiệu rời rạc** trong miền \( \omega \in [-\pi, \pi] \).