<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đăng ký - Sentiment Insight AI"/>
<jsp:include page="header.jsp"/>

<div class="row justify-content-center mt-5">
  <div class="col-12 col-md-6 col-lg-5">
    <div class="card p-4">
      <h4 class="mb-3 text-center">Đăng ký</h4>
      <form action="<%= request.getContextPath() %>/register" method="post" class="needs-validation" novalidate>
        <div class="mb-3">
          <label for="fullName" class="form-label">Họ tên</label>
          <input type="text" class="form-control" id="fullName" name="fullName" required>
        </div>
        <div class="mb-3">
          <label for="email" class="form-label">Email</label>
          <input type="email" class="form-control" id="email" name="email" required>
        </div>
        <div class="mb-3">
          <label for="password" class="form-label">Mật khẩu</label>
          <input type="password" class="form-control" id="password" name="password" required>
        </div>
        <div class="mb-3">
          <label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
          <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
        </div>
        <button type="submit" class="btn btn-primary w-100">Đăng ký</button>
      </form>
      <div class="text-center mt-3">
        <a href="<%= request.getContextPath() %>/login">Đã có tài khoản? Đăng nhập</a>
      </div>
      <c:if test="${not empty requestScope.error}">
        <div class="alert alert-danger mt-3" role="alert">${error}</div>
      </c:if>
    </div>
  </div>
  </div>

<jsp:include page="footer.jsp"/>

