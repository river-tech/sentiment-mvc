<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Dashboard - Sentiment Insight AI"/>
<jsp:include page="header.jsp"/>

<div class="row mb-4">
  <div class="col-12 col-lg-8">
    <div class="card p-4">
      <h5 class="mb-3">Phân tích cảm xúc theo từ khóa</h5>
      <form id="analyzeForm" action="<%= request.getContextPath() %>/jobs/create" method="post" class="row g-2">
        <div class="col-12 col-md-9">
          <input type="text" class="form-control" name="keyword" id="keywordInput" placeholder="Nhập từ khóa..." required>
        </div>
        <div class="col-12 col-md-3 d-grid">
          <button class="btn btn-primary" type="submit" id="submitBtn">
            <span id="submitText">Phân tích cảm xúc</span>
            <span id="submitSpinner" class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
          </button>
        </div>
      </form>
    </div>
  </div>
  <div class="col-12 col-lg-4">
    <div class="card p-4">
      <h6 class="mb-3">Nền tảng phân tích cảm xúc thời gian thực</h6>
      <p class="text-muted mb-0">Nhập từ khóa để hệ thống thu thập bài viết và phân tích cảm xúc, kết quả được chia sẻ toàn hệ thống.</p>
    </div>
  </div>
</div>

<!-- Loading Overlay -->
<div id="loadingOverlay" style="display:none; position:fixed; inset:0; background:rgba(255,255,255,0.9); z-index:1050; align-items:center; justify-content:center; flex-direction:column;">
  <div class="spinner-border text-primary mb-3" role="status" style="width:4rem;height:4rem;">
    <span class="visually-hidden">Loading...</span>
  </div>
  <div class="fw-semibold fs-5">Đang tìm kiếm và phân tích...</div>
  <div class="text-muted mt-2">Vui lòng đợi trong giây lát</div>
</div>

<div id="results" class="row section">
  <div class="col-12 mb-3">
    <div class="card p-3">
      <div class="d-flex align-items-center justify-content-between">
        <div>
          <h6 class="mb-1">Trạng thái xử lý</h6>
          <small class="text-muted" id="statusKeyword"></small>
        </div>
        <div>
          <span id="jobStatusBadge" class="badge fs-6 px-3 py-2">Chưa có dữ liệu</span>
        </div>
      </div>
      <div class="mt-2">
        <div class="progress" style="height: 8px;">
          <div id="jobProgress" class="progress-bar" role="progressbar" style="width: 0%" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div>
        </div>
      </div>
    </div>
  </div>
  <div class="col-12 col-lg-4">
    <div class="card p-4">
      <h6 class="mb-3">Tỷ lệ cảm xúc</h6>
      <canvas id="sentimentChart" width="400" height="200"></canvas>
    </div>
  </div>
  <div class="col-12 col-lg-8">
    <div class="card p-3">
      <div class="table-responsive" style="max-height: 600px; overflow-y: auto;">
      <table class="table table-striped align-middle table-hover mb-0">
        <thead class="table-primary sticky-top">
          <tr>
            <th style="width: 35%; min-width: 200px;">Tiêu đề</th>
            <th style="width: 50%; min-width: 250px;">Tóm tắt</th>
            <th class="text-center" style="width: 15%; min-width: 100px;">Cảm xúc</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="a" items="${articles}">
            <tr>
              <td style="word-wrap: break-word; word-break: break-word;">
                <a href="${a.url}" target="_blank" class="text-decoration-none fw-semibold" style="color: #0d6efd;">
                  ${a.title}
                </a>
              </td>
              <td style="word-wrap: break-word; word-break: break-word; font-size: 0.9rem; color: #6c757d;" class="article-desc">
                ${a.description}
              </td>
              <td class="text-center">
                <c:set var="sentimentLower" value="${fn:toLowerCase(a.sentiment)}" />
                <c:choose>
                  <c:when test="${sentimentLower == 'positive'}">
                    <span class="badge sentiment-badge sentiment-positive" style="background-color: #10b981 !important; color: white !important;">Tích cực</span>
                  </c:when>
                  <c:when test="${sentimentLower == 'negative'}">
                    <span class="badge sentiment-badge sentiment-negative" style="background-color: #ef4444 !important; color: white !important;">Tiêu cực</span>
                  </c:when>
                  <c:otherwise>
                    <span class="badge sentiment-badge sentiment-neutral" style="background-color: #f59e0b !important; color: white !important;">Trung lập</span>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty articles}">
            <tr><td colspan="3" class="text-center text-muted py-4">Chưa có dữ liệu hiển thị</td></tr>
          </c:if>
        </tbody>
      </table>
      </div>
    </div>
  </div>
</div>

<script>
  (function(){
    let chartInstance = null;
    const form = document.getElementById('analyzeForm');
    const overlay = document.getElementById('loadingOverlay');
    const results = document.getElementById('results');
    const submitBtn = document.getElementById('submitBtn');
    const submitText = document.getElementById('submitText');
    const submitSpinner = document.getElementById('submitSpinner');
    const keywordInput = document.getElementById('keywordInput');
    const ctx = document.getElementById('sentimentChart');
    const tbody = document.querySelector('#results tbody');

    // Clear UI on page load
    function clearUI() {
      if (chartInstance) {
        chartInstance.destroy();
        chartInstance = null;
      }
    if (ctx) {
        chartInstance = new Chart(ctx, {
          type: 'pie',
          data: {
        labels: ['Tích cực', 'Tiêu cực', 'Trung lập'],
            datasets: [{ 
              label: 'Kết quả cảm xúc (%)', 
              data: [0, 0, 0], 
              backgroundColor: ['#10b981', '#ef4444', '#f59e0b'] 
            }]
          }
        });
      }
      if (tbody) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Chưa có dữ liệu hiển thị</td></tr>';
      }
      
      // Reset status badge and progress
      const statusBadge = document.getElementById('jobStatusBadge');
      const statusKeyword = document.getElementById('statusKeyword');
      const jobProgress = document.getElementById('jobProgress');
      
      if (statusBadge) {
        statusBadge.className = 'badge fs-6 px-3 py-2 bg-secondary';
        statusBadge.textContent = 'Chưa có dữ liệu';
      }
      if (statusKeyword) {
        statusKeyword.textContent = '';
      }
      if (jobProgress) {
        jobProgress.style.width = '0%';
        jobProgress.setAttribute('aria-valuenow', '0');
        jobProgress.className = 'progress-bar bg-secondary';
      }
    }

    // Always clear UI on page load/refresh
    clearUI();
    
    // Strip HTML from description cells on page load
    function stripHtmlFromDescriptions() {
      const descCells = document.querySelectorAll('.article-desc');
      descCells.forEach(function(cell) {
        if (cell.innerHTML) {
          const tmp = document.createElement('div');
          tmp.innerHTML = cell.innerHTML;
          let text = tmp.textContent || tmp.innerText || '';
          text = text.replace(/\s+/g, ' ').trim();
          if (text.length > 200) {
            text = text.substring(0, 200) + '...';
          }
          cell.textContent = text;
        }
      });
    }
    
    // Run on page load
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', stripHtmlFromDescriptions);
    } else {
      stripHtmlFromDescriptions();
    }

    // Show loading state
    function showLoading() {
      if (overlay) overlay.style.display = 'flex';
      if (submitBtn) submitBtn.disabled = true;
      if (submitText) submitText.textContent = 'Đang xử lý...';
      if (submitSpinner) submitSpinner.classList.remove('d-none');
    }

    // Hide loading state
    function hideLoading() {
      if (overlay) overlay.style.display = 'none';
      if (submitBtn) submitBtn.disabled = false;
      if (submitText) submitText.textContent = 'Phân tích cảm xúc';
      if (submitSpinner) submitSpinner.classList.add('d-none');
    }

    // Get status badge class and text
    function getStatusBadge(status) {
      if (!status) return { class: 'bg-secondary', text: 'Chưa có dữ liệu' };
      const s = status.toUpperCase();
      if (s === 'DONE') return { class: 'bg-success', text: 'Hoàn thành' };
      if (s === 'RUNNING') return { class: 'bg-primary', text: 'Đang xử lý' };
      if (s === 'QUEUED') return { class: 'bg-info text-dark', text: 'Đang chờ' };
      if (s === 'FAILED') return { class: 'bg-danger', text: 'Thất bại' };
      return { class: 'bg-secondary', text: status };
    }

    // Get sentiment badge class and text
    function getSentimentBadge(sentiment) {
      if (!sentiment) return { class: 'sentiment-neutral', text: 'Trung lập' };
      const s = sentiment.toLowerCase();
      if (s === 'positive') return { class: 'sentiment-positive', text: 'Tích cực' };
      if (s === 'negative') return { class: 'sentiment-negative', text: 'Tiêu cực' };
      return { class: 'sentiment-neutral', text: 'Trung lập' };
    }

    // Update UI with data
    function updateUI(data) {
      if (!data) return;
      
      // Update job status
      const statusBadge = document.getElementById('jobStatusBadge');
      const statusKeyword = document.getElementById('statusKeyword');
      const jobProgress = document.getElementById('jobProgress');
      
      if (statusBadge && data.status) {
        const statusInfo = getStatusBadge(data.status);
        statusBadge.className = 'badge fs-6 px-3 py-2 ' + statusInfo.class;
        statusBadge.textContent = statusInfo.text;
      }
      
      if (statusKeyword && data.keyword) {
        statusKeyword.textContent = 'Từ khóa: ' + data.keyword;
      }
      
      if (jobProgress && data.progress != null) {
        const progress = Math.min(100, Math.max(0, Number(data.progress) || 0));
        jobProgress.style.width = progress + '%';
        jobProgress.setAttribute('aria-valuenow', progress);
        
        // Update progress bar color based on status
        jobProgress.className = 'progress-bar';
        if (data.status) {
          const s = data.status.toUpperCase();
          if (s === 'DONE') jobProgress.classList.add('bg-success');
          else if (s === 'RUNNING') jobProgress.classList.add('bg-primary');
          else if (s === 'QUEUED') jobProgress.classList.add('bg-info');
          else if (s === 'FAILED') jobProgress.classList.add('bg-danger');
          else jobProgress.classList.add('bg-secondary');
        }
      }
      
      // Update chart
      if (chartInstance && (data.positive != null || data.negative != null || data.neutral != null)) {
        chartInstance.data.datasets[0].data = [
          Number(data.positive || 0), 
          Number(data.negative || 0), 
          Number(data.neutral || 0)
        ];
        chartInstance.update();
      }

      // Update table
      if (tbody && Array.isArray(data.articles)) {
        if (data.articles.length === 0) {
          tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-4">Chưa có dữ liệu hiển thị</td></tr>';
        } else {
          let html = '';
          data.articles.forEach(function(a) {
            const url = escapeHtml(a.url || '');
            const title = escapeHtml(a.title || '');
            // Strip HTML tags from description and truncate if too long
            let desc = stripHtml(a.description || '');
            if (desc.length > 200) {
              desc = desc.substring(0, 200) + '...';
            }
            desc = escapeHtml(desc);
            const sentimentInfo = getSentimentBadge(a.sentiment);
            let badgeStyle = '';
            if (sentimentInfo.class === 'sentiment-positive') {
              badgeStyle = 'background-color: #10b981 !important; color: white !important;';
            } else if (sentimentInfo.class === 'sentiment-negative') {
              badgeStyle = 'background-color: #ef4444 !important; color: white !important;';
            } else {
              badgeStyle = 'background-color: #f59e0b !important; color: white !important;';
            }
            html += '<tr>' +
              '<td style="word-wrap: break-word; word-break: break-word;">' +
              '<a href="' + url + '" target="_blank" class="text-decoration-none fw-semibold" style="color: #0d6efd;">' + title + '</a>' +
              '</td>' +
              '<td style="word-wrap: break-word; word-break: break-word; font-size: 0.9rem; color: #6c757d;">' + desc + '</td>' +
              '<td class="text-center">' +
              '<span class="badge sentiment-badge ' + sentimentInfo.class + '" style="' + badgeStyle + '">' + sentimentInfo.text + '</span>' +
              '</td>' +
              '</tr>';
          });
          tbody.innerHTML = html;
        }
      }
    }

    function escapeHtml(str) {
      if (!str) return '';
      const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
      return String(str).replace(/[&<>"']/g, m => map[m]);
    }

    function stripHtml(html) {
      if (!html) return '';
      // Create a temporary div element
      const tmp = document.createElement('div');
      tmp.innerHTML = html;
      // Get text content and clean up whitespace
      let text = tmp.textContent || tmp.innerText || '';
      // Clean up multiple spaces and newlines
      text = text.replace(/\s+/g, ' ').trim();
      return text;
    }

    // Handle form submission
    if (form) {
      form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Get keyword value FIRST before any UI changes
        const keywordValue = keywordInput ? keywordInput.value.trim() : '';
        if (!keywordValue) {
          alert('Vui lòng nhập từ khóa');
          if (keywordInput) keywordInput.focus();
          return;
        }

        // Clear UI before new search
        clearUI();
        showLoading();

        // Log for debugging
        console.log('Submitting keyword:', keywordValue);

        // Use URLSearchParams instead of FormData for better compatibility
        const params = new URLSearchParams();
        params.append('keyword', keywordValue);
        params.append('ajax', '1');
        
        // Debug: log all params
        console.log('Request params:', params.toString());

        try {
          const res = await fetch(form.action, {
            method: 'POST',
            headers: { 
              'X-Requested-With': 'XMLHttpRequest',
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
          });
          
          if (!res.ok) {
            const text = await res.text();
            console.error('Server error:', res.status, text);
            hideLoading();
            alert('Lỗi server: ' + res.status + '. Vui lòng kiểm tra console để xem chi tiết.');
            return;
          }

          const contentType = res.headers.get('content-type');
          if (!contentType || !contentType.includes('application/json')) {
            const text = await res.text();
            console.error('Response is not JSON:', text.substring(0, 200));
            hideLoading();
            alert('Server trả về dữ liệu không đúng định dạng. Vui lòng kiểm tra console.');
            return;
          }

          const data = await res.json();
          console.log('Response data:', data);
          
          // Check for error in response
          if (data.error) {
            hideLoading();
            alert('Lỗi: ' + data.error);
            return;
          }
          
          if (!data || !data.jobId) {
            hideLoading();
            alert('Không thể tạo job. Server trả về: ' + JSON.stringify(data));
            return;
          }

          // Get and validate jobId
          let jobId = data.jobId;
          console.log('Raw jobId from server:', jobId, 'Type:', typeof jobId);
          
          // Convert to number if needed, but keep as string for URL
          if (jobId != null) {
            jobId = String(jobId).trim();
          }
          
          // Validate jobId
          if (!jobId || jobId === '' || jobId === 'null' || jobId === 'undefined') {
            console.error('Invalid jobId:', jobId, 'Full data:', data);
            hideLoading();
            alert('Lỗi: Không có jobId hợp lệ. Server trả về: ' + JSON.stringify(data));
            return;
          }
          
          console.log('Starting to poll job status for jobId:', jobId);

          // Poll status until DONE or FAILED with timeout
          let pollCount = 0;
          const maxPolls = 120; // 120 * 1.5s = 3 minutes max
          let pollTimeoutId = null;
          const contextPath = '<%= request.getContextPath() %>';
          
          const poll = async () => {
            try {
              pollCount++;
              console.log(`Polling attempt ${pollCount}/${maxPolls} for jobId: ${jobId}`);
              
              // Build URL safely
              const statusUrl = contextPath + '/jobs/status?id=' + encodeURIComponent(jobId);
              console.log('Fetching status from:', statusUrl);
              
              const sres = await fetch(statusUrl, { 
                headers: { 'X-Requested-With': 'XMLHttpRequest' } 
              });
              
              if (!sres.ok) {
                console.error('Status check failed:', sres.status, sres.statusText);
                if (pollCount >= maxPolls) {
                  hideLoading();
                  alert('Timeout: Không thể lấy trạng thái job sau ' + maxPolls + ' lần thử.');
                  return;
                }
                setTimeout(poll, 1500);
                return;
              }

              const sdata = await sres.json();
              console.log('Status response:', sdata);
              
              if (sdata && sdata.status) {
                updateUI(sdata);
                
                const status = sdata.status.toUpperCase();
                console.log('Current status:', status);
                
                if (status === 'DONE' || status === 'FAILED') {
                  console.log('Job completed with status:', status);
                  hideLoading();
                  if (status === 'FAILED') {
                    alert('Job xử lý thất bại. Vui lòng thử lại.');
                  } else {
                    console.log('Job completed successfully!');
                  }
                  if (pollTimeoutId) clearTimeout(pollTimeoutId);
                  return;
                }
              } else {
                console.warn('No status in response:', sdata);
              }
              
              // Check timeout
              if (pollCount >= maxPolls) {
                console.error('Max polls reached, stopping');
                hideLoading();
                alert('Timeout: Job đang xử lý quá lâu. Vui lòng refresh trang để kiểm tra.');
                return;
              }
              
              // Continue polling
              pollTimeoutId = setTimeout(poll, 1500);
              
            } catch (err) {
              console.error('Poll error:', err);
              if (pollCount >= maxPolls) {
                hideLoading();
                alert('Lỗi khi kiểm tra trạng thái sau ' + maxPolls + ' lần thử. Vui lòng refresh trang.');
              } else {
                // Retry on error
                pollTimeoutId = setTimeout(poll, 1500);
              }
            }
          };
          
          // Start polling
          poll();
          
          // Safety timeout: force hide loading after 5 minutes
          setTimeout(() => {
            if (overlay && overlay.style.display !== 'none') {
              console.warn('Force hiding loading after 5 minutes');
              hideLoading();
              alert('Job đang xử lý quá lâu. Vui lòng refresh trang để kiểm tra kết quả.');
            }
          }, 300000); // 5 minutes
        } catch (err) {
          console.error('Submit error:', err);
          hideLoading();
          alert('Lỗi khi gửi request. Vui lòng thử lại.');
        }
      });
    }
  })();
</script>

<jsp:include page="footer.jsp"/>

