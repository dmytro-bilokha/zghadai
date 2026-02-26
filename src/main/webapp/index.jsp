<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<body>
<h2>Hello World from JSP!</h2>
<p>Use this <a href="<%=response.encodeURL(application.getContextPath()+"/HelloWorld")%>">application.getContextPath() link</a> to see servlet greeting</p>
<p>Or use <a href="${pageContext.request.contextPath}/HelloWorld">\${application.contextPath} link</a> to see servlet greeting</p>
<p>Or  this <a href="<c:url value="/HelloWorld" />">c:url link</a> to see servlet greeting</p>
</body>
</html>
