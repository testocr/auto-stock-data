package org.tloss.translate;

import org.tloss.common.Article;
/**
 * 
 * @author tungt
 *
 */
public interface Translate {
	Article transalte(Article article,String lang1,String lang2) throws Exception;
	String translate(String data,String lang1,String lang2) throws Exception;
}
