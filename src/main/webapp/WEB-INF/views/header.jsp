<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${pageTitle}" default="Sentiment Insight AI"/></title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body style="font-family: 'Inter', sans-serif;">
<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm sticky-top">
  <div class="container">
    <a class="navbar-brand fw-semibold text-primary" href="#">Sentiment Insight AI</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav ms-auto">
        <c:choose>
          <c:when test="${not empty sessionScope.user}">
            <li class="nav-item"><a class="nav-link" href="<%= request.getContextPath() %>/dashboard">Dashboard</a></li>
            <li class="nav-item"><a class="btn btn-outline-danger ms-2" href="<%= request.getContextPath() %>/logout">Đăng xuất</a></li>
          </c:when>
          <c:otherwise>
            <li class="nav-item"><a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/register">Đăng ký</a></li>
            <li class="nav-item"><a class="btn btn-primary ms-2" href="<%= request.getContextPath() %>/login">Đăng nhập</a></li>
          </c:otherwise>
        </c:choose>
      </ul>
    </div>
  </div>
  </nav>
<main class="container my-4">

