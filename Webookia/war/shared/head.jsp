<%@page import="it.webookia.backend.utils.Settings"%>
<%@page import="it.webookia.backend.controller.resources.BookResource"%>
<%@page import="it.webookia.backend.controller.services.Books"%>
<%@page import="it.webookia.backend.descriptor.BookDescriptor"%>
<%@page import="it.webookia.backend.utils.ServletUtils"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%
	String pageTitle = "Webookia";

	BookResource hBook = ServletUtils.getRequestAttribute(request,
			BookResource.class, Books.CONTEXT_BOOK);
	BookDescriptor hContextBook = (hBook == null) ? null : hBook
			.getDescriptor();
%>

<head>
<title><%=pageTitle%></title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="description" content="" />
<meta name="keywords" content="" />

<link type="text/css" rel="stylesheet" href="/css/zero.css" />
<link type="text/css" rel="stylesheet" href="/css/clearfix.css" />
<link href='http://fonts.googleapis.com/css?family=Droid+Sans|Alef'
	rel='stylesheet' type='text/css'>
<link type="text/css" rel="stylesheet" href="/css/webookia.css" />
<link rel="icon" href="/favicon.ico" type="image/icon" />
<script type="text/javascript"
	src="http://code.jquery.com/jquery-1.10.1.js"></script>
<script type="text/javascript"
	src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
<script type="text/javascript" src="/js/Webookia.Core.js"></script>
<%
	if (hContextBook != null) {
%>
<meta property="og:type" content="book" />
<meta property="og:url"
	content="<%=Settings.CURRENT_HOST%>/books/detail?id=<%=hContextBook.getId()%>" />
<meta property="og:title" content="<%=hContextBook.getTitle()%>" />
<meta property="og:image" content="<%=hContextBook.getThumbnail()%>" />
<meta property="og:site_name" content="Webookia" />
<meta property="og:app_id" content="497944510260906" />
<%
	}
%>

</head>