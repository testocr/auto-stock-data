package org.tloss.agpush;

import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;

public class AGPush {
	private AGPush() {

	}

	private static AGPush instance = null;

	public static synchronized AGPush getInstance() {
		if (instance == null) {
			instance = new AGPush();
		}
		return instance;
	}

	private JavaSender defaultJavaSender = new SenderClient.Builder(
			"aerogear-gearpush.rhcloud.com/ag-push/").build();
	public void sendErrorNotification(String info, Exception e) {
		UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
				.pushApplicationId("d7ebb4ad-1cda-40a8-8bbb-8fe958d636f3")
				.masterSecret("68dc7315-410e-447f-b242-6e7c90b04944")
				.alert(info + " : " + e.getMessage()).build();
		defaultJavaSender.send(unifiedMessage, new MessageResponseCallback() {

			public void onComplete(int statusCode) {
				// do cool stuff
			}

			public void onError(Throwable throwable) {
				// bring out the bad news
				throwable.fillInStackTrace();
			}
		});
	}

	public void sendNotification(String info) {

		UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
				.pushApplicationId("d7ebb4ad-1cda-40a8-8bbb-8fe958d636f3")
				.masterSecret("68dc7315-410e-447f-b242-6e7c90b04944")
				.alert(info).build();
		defaultJavaSender.send(unifiedMessage, new MessageResponseCallback() {

			public void onComplete(int statusCode) {
				// do cool stuff
			}

			public void onError(Throwable throwable) {
				// bring out the bad news
				throwable.printStackTrace();
			}
		});
	}

}
