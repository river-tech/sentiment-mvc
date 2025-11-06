<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Jobs - Sentiment MVC</title>
</head>
<body>
    <h2>Jobs Page</h2>
    
    <nav>
        <a href="<%= request.getContextPath() %>/dashboard">Dashboard</a> |
        <a href="<%= request.getContextPath() %>/jobs/create">Create New Job</a>
    </nav>
    
    <h3>Job List</h3>
    <table border="1">
        <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Status</th>
                <th>Created At</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <!-- TODO: Display jobs from request attribute -->
            <!-- Example:
            <c:forEach var="job" items="${jobs}">
                <tr>
                    <td><c:out value="${job.id}" /></td>
                    <td><c:out value="${job.name}" /></td>
                    <td><c:out value="${job.status}" /></td>
                    <td><c:out value="${job.createdAt}" /></td>
                    <td>
                        <a href="<%= request.getContextPath() %>/jobs/${job.id}">View</a>
                        <a href="<%= request.getContextPath() %>/jobs/${job.id}/delete">Delete</a>
                    </td>
                </tr>
            </c:forEach>
            -->
            <tr>
                <td colspan="5">No jobs found</td>
            </tr>
        </tbody>
    </table>
    
    <!-- TODO: Add pagination -->
    <!-- TODO: Add filter by status -->
    <!-- TODO: Add session check -->
</body>
</html>

