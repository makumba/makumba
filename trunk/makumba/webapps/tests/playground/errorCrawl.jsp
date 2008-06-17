<%@page import="java.util.ArrayList"%>
<%@page import="org.makumba.devel.relations.RelationCrawler"%>
<%
RelationCrawler rc = RelationCrawler.getRelationCrawler("/home/projects/parade/PAtest/karamba", "localhost_mysql_TESTkaramba", false);

ArrayList<String> toCrawl = RelationCrawler.getAllFilesInDirectory("/home/projects/parade/PAtest/karamba");

for(String path : toCrawl) {
	rc.crawl(path);
}

rc.writeJSPAnalysisError("/home/projects/parade/PAtest/karamba-errors.txt");
%>