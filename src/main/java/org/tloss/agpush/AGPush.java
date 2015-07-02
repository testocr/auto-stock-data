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

	private String AppID = "8125ba60-3b6d-408e-8b30-8a22cd39bba9";
	private String masterKey = "485620e2-59b3-46bc-917c-f25643efef55";
	private String URL = "https://aerogear-gearpush.rhcloud.com/ag-push/";
	private JavaSender defaultJavaSender = new SenderClient.Builder(URL)
			.build();

	public void sendErrorNotification(String info, Exception e) {
		UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
				.pushApplicationId(AppID).masterSecret(masterKey)
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
				.pushApplicationId(AppID)
				.masterSecret(masterKey)
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
