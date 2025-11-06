<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Dashboard - Sentiment Insight AI"/>
<jsp:include page="header.jsp"/>

<div class="row mb-4">
  <div class="col-12 col-lg-8">
    <div class="card p-4">
      <h5 class="mb-3">Phân tích cảm xúc theo từ khóa</h5>
      <form action="<%= request.getContextPath() %>/jobs/create" method="post" class="row g-2">
        <div class="col-12 col-md-9">
          <input type="text" class="form-control" name="keyword" placeholder="Nhập từ khóa..." required>
        </div>
        <div class="col-12 col-md-3 d-grid">
          <button class="btn btn-primary" type="submit">Phân tích cảm xúc</button>
        </div>
      </form>
    </div>
  </div>
  <div class="col-12 col-lg-4">
    <div class="card p-4">
      <h6 class="mb-3">Xin chào, <c:out value="${sessionScope.user.username}" default="Khách"/></h6>
      <p class="text-muted mb-0">Nhập từ khóa để hệ thống thu thập bài viết và phân tích cảm xúc.</p>
    </div>
  </div>
</div>

<div class="row section">
  <div class="col-12 col-lg-4">
    <div class="card p-4">
      <h6 class="mb-3">Tỷ lệ cảm xúc</h6>
      <canvas id="sentimentChart" width="400" height="200"></canvas>
    </div>
  </div>
  <div class="col-12 col-lg-8">
    <div class="card p-3">
      <div class="table-responsive">
      <table class="table table-striped align-middle">
        <thead class="table-primary">
          <tr>
            <th>Tiêu đề</th>
            <th>Tóm tắt</th>
            <th class="text-center">Cảm xúc</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="a" items="${articles}">
            <tr>
              <td><a href="${a.url}" target="_blank">${a.title}</a></td>
              <td>${a.description}</td>
              <td class="text-center">
                <span class="badge ${a.sentiment == 'positive' ? 'bg-success' : (a.sentiment == 'negative' ? 'bg-danger' : 'bg-warning text-dark')}">
                  ${a.sentiment}
                </span>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty articles}">
            <tr><td colspan="3" class="text-center text-muted">Chưa có dữ liệu hiển thị</td></tr>
          </c:if>
        </tbody>
      </table>
      </div>
    </div>
  </div>
</div>

<script>
  (function(){
    const pos = Number("${job.positive != null ? job.positive : 0}");
    const neg = Number("${job.negative != null ? job.negative : 0}");
    const neu = Number("${job.neutral != null ? job.neutral : 0}");
    const ctx = document.getElementById('sentimentChart');
    if (ctx) {
      const data = {
        labels: ['Tích cực', 'Tiêu cực', 'Trung lập'],
        datasets: [{ label: 'Kết quả cảm xúc (%)', data: [pos, neg, neu], backgroundColor: ['#4CAF50', '#F44336', '#FFC107'] }]
      };
      new Chart(ctx, { type: 'pie', data });
    }
  })();
</script>

<jsp:include page="footer.jsp"/>

