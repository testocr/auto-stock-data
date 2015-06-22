package org.tloss.stock.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
	public static class Result {
		int startIndex;
		int endIndex;
		String result;

		public int getStartIndex() {
			return startIndex;
		}

		public void setStartIndex(int startIndex) {
			this.startIndex = startIndex;
		}

		public int getEndIndex() {
			return endIndex;
		}

		public void setEndIndex(int endIndex) {
			this.endIndex = endIndex;
		}

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}

	}

	public static Result search(String content, int startIndex,
			String begin, String end) {
		Result result = new Result();
		int s = content.indexOf(begin, startIndex);
		if (s >= startIndex ) {
			int e = content.indexOf(end, s + begin.length());
			if (e >= 0 ) {
				result.setResult(content.substring(s, e + end.length()));
				result.setStartIndex(s);
				result.setEndIndex(e);
			}
		}
		return result;
	}

	public static List<Result> searchs(String content, int startIndex,
			 String begin, String end) {
		List<Result> result = new ArrayList<StringUtils.Result>();
		int ts = startIndex;
		Result rs;
		boolean stop = false;
		while (!stop) {
			rs = search(content, ts,  begin, end);
			if (rs.getResult() != null) {
				result.add(rs);
				ts = rs.endIndex + end.length();
			} else {
				stop = true;
			}

		}
		return result;
	}
}
