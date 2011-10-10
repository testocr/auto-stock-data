package org.tloss.multiget;

public interface AutoGetArticle extends GetArticle {
	boolean isNew(String url,Object[] data) throws Exception;
	public String[] getDeafaltListUrl();
	
	
}
