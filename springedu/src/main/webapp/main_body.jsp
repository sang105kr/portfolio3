<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <main role="main" class="container">
  
			<c:if test="${user ne null }">
				<b>로그아웃</b>
			</c:if>
			<c:if test="${user eq null }">
				<b>로그인</b>
			</c:if>
		<hr />
			<c:choose>
				<c:when test="${user ne null }">
					<b>로그아웃</b>
				</c:when>
				<c:when test="${user eq null }">
					<b>로그인</b>
				</c:when>
			</c:choose>
		<hr />	
			<c:choose>
				<c:when test="${sessionScope.user ne null }">
					<b>로그아웃</b>
				</c:when>
				<c:otherwise>
					<b>로그인</b>
				</c:otherwise>
			</c:choose>			
		<hr />		
			${user.id } <br>
			${user.pw } <br>
			${user.nickName } <br>
			${user.birth } <br>
			${user.gender } <br>
			${user.tel } <br>
			${user.region } <br>
			
			
		</main><!-- /.container -->