package org.tloss.multipos;

import org.tloss.common.Article;

public interface PostArticle {
	public static final int LOGIN_FORM_URL = 0;
	public static final int LOGIN_POST_URL = 1;
	public static final int POST_FORM_URL = 2;
	public static final int POST_URL = 3;
	public static final int LOGOUT_URL = 4;

	public boolean login(String username, String password) throws Exception;

	public String getUrl(int type, Object[] options);

	public boolean login(String username, String password,
			boolean encrytedPassword, Object[] options) throws Exception;

	public boolean post(Article article, String urlEdit, String urlPost,
			Object[] options) throws Exception;

	public void logout();
}
