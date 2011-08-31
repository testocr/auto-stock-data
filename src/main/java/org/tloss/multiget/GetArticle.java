package org.tloss.multiget;

import org.tloss.common.Article;

public interface GetArticle {
	public boolean login(String username, String password) throws Exception;
	public boolean login(String username,String password,boolean encrytedPassword,Object[] options) throws Exception;
	public Article get(String url)  throws Exception ;
	public Article[] getAll(String url)  throws Exception ;

	public void logout();
}
