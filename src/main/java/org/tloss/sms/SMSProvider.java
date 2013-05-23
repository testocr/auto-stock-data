package org.tloss.sms;

public interface SMSProvider {
	public boolean sendSms(String mobile, String text) throws Exception;

	public int getLeftFreeSms() throws Exception;

	public boolean isLogined() throws Exception;

	public boolean login(String user, String password) throws Exception;

}
