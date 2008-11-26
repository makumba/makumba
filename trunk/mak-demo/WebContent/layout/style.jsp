<%@page contentType="text/css"
%><%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" 
%><c:set var="path" value="${pageContext.request.contextPath}" scope="request" />

body
{
	background: #444 url('${path}/images/bg_pattern.png') 0px -1px;
	margin: 0;
	padding: 0;
	font-family: Arial, Helvetica;
	font-size: 12px;
}

table
{
	font-size: 12px;
}

th
{
  background: #CCFF66;
}

td
{
  text-align:left;
  vertical-align:top;
}

table.forum
{
  width:100%;
}

td.forumdetails
{
  text-align:right;
  width:200px;
  font-size:9px;
  font-style:italic;
  color: #666;
}
td.forumbottom
{
  text-align:right;
}


hr 
{
  color: #055a05;
  border-top: solid 1px #055a05;
  border-bottom: none;
  height: 0px;
}

a
{
	color: #055a05;
}

div.forumQuoter
{
  background: #dcf168;
  margin-right:3px;
  width:7px;
  float:left;
}
div.pagination
{
  width:100%;
  text-align:center;
  padding:4px;
  margin: 4px 0px;
}
div.pagination span
{
   border: 1px solid #055a05;
   padding:0px 4px;
}
div.pagination span.page:hover
{
    background: red;
}

div.pagination span.activePage
{
   background: black;
   color:white;
}
#ubercontainer
{
	background: url('${path}/images/gradient_top.png') repeat-x 0px -3px;
	padding: 20px 0;
	text-align: center;
	min-height: 400px;
}

#container
{
	background: #fff;
	margin: 0 auto;
	text-align: left;
	width: 930px !important;
	width: 950px;
	padding: 10px;
}

#logo_bg
{
	background: url('${path}/images/logo_bg.jpg') no-repeat;
	height: 220px;
	margin-bottom: 10px;
}

#logo_bg a
{
	color: #fff;
	text-decoration: none;
}

#logo_bg h1
{
	padding-top: 80px;
	color: #fff;
	text-align: center;
	margin: 0;
	font-size: 30px;
}
#logo_bg h2
{
	font-size: 14px;
	color: #fff;
	margin: 0;
	padding-top: 10px;
	text-align: center;
}

#right_menu
{
	float: right;
	width: 220px !important;
	width: 240px;
	padding: 10px;
	/*background: #eee;*/
}

#right_menu h3
{
	margin: 0;
	color: #055a05;
	border-bottom: 1px solid #055a05;
}

#right_menu ul
{
	margin-bottom: 20px;
	padding-left: 20px;
}

#back_clear
{
	clear: both;
}

#content
{
	width: 640px !important;
	width: 680px;
	min-height: 400px;
	padding: 20px;
}

#content h1
{
	margin: 0;
	color: #055a05;
	border-bottom: 1px solid #055a05;
}

#content .blogpost
{
	font-size: 13px;
	margin-bottom: 40px;
} 
#content .blogpost em
{
	padding-top: 10px;
	display: block;
	margin-left: 20px;
	font-size: 12px;
	color: #888;
}

#close
{
	background: url('${path}/images/back_bg.jpg') no-repeat bottom;
	padding-top: 10px;
	height: 25px !important;
	height: 35px;
	width: 100%;
	margin-top: 10px;
	color: #fff;
	text-align: center;
}
#close a
{
	color: #fff;
}
#close a:hover
{
	color: #aaa;
}

div.mak_response
{
  border: 1px dashed #055a05;
  background: #e4ffb1;
  width:100%;
  text-align:center;
  padding:5px;
  color: white;
}

.description
{
  font-size:11px;
  font-style:italic;
  color: #666;
}

.makumbaPagination
{
	margin-bottom: 30px;
	border-top: 1px solid #055a05;
	padding-top: 10px;
	border-bottom: 1px solid #055a05;
	padding-bottom: 6px;
	
}

.LV_validation_message
{
	margin-left: 5px;
}

