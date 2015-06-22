package org.tloss.stock.utils;

import java.util.ArrayList;
import java.util.List;

import org.tloss.stock.utils.StringUtils.Result;

public class HtmlUtils {

	public static Form parseForm(String content, int startIndex, String begin,
			String end) {
		Form form = null;
		Result result = StringUtils.search(content, startIndex, begin, end);
		String formContent = result.getResult();
		if (formContent != null) {
			form = new Form();
			form.setFormContent(formContent);
			form.setAction(getAttributeValue("action", formContent));
			form.setMethod(getAttributeValue("method", formContent));
		}
		return form;
	}

	public static String getAttributeValue(String attributeName, String content) {
		Result result;
		String value = null;
		result = StringUtils.search(content, 0, attributeName + "=\"", "\"");
		if (result.getResult() != null) {
			value = content.substring(result.startIndex
					+ (attributeName + "=\"").length(), result.endIndex);
		} else {
			result = StringUtils.search(content, 0, attributeName + "='", "'");
			if (result.getResult() != null) {
				value = content.substring(result.startIndex
						+ (attributeName + "='").length(), result.endIndex);
			}
		}
		return value;
	}

	public static void parseFormField(String content, int startIndex,
			String begin, String end, Form form) {
		List<Result> results = StringUtils.searchs(content, startIndex, begin,
				end);
		Input input;
		for (Result result : results) {
			input = new Input();
			input.setName(getAttributeValue("name", result.getResult()));
			input.setValue(getAttributeValue("value", result.getResult()));
			input.setType(getAttributeValue("type", result.getResult()));
			input.setInputContent(result.getResult());
			form.getInputs().add(input);
		}
	}

	public static class Form {
		String action;
		String method;
		String formContent;
		
		List<Input> inputs= new ArrayList<HtmlUtils.Input>();

		
		public String getFormContent() {
			return formContent;
		}

		public void setFormContent(String formContent) {
			this.formContent = formContent;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public List<Input> getInputs() {
			return inputs;
		}

		public void setInputs(List<Input> inputs) {
			this.inputs = inputs;
		}

	}

	public static class Input {
		String name;
		String value;
		String type;
		String inputContent;
		public void setInputContent(String inputContent) {
			this.inputContent = inputContent;
		}
		public String getInputContent() {
			return inputContent;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}
}
