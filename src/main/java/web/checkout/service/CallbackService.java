package web.checkout.service;

import java.util.Map;

public interface CallbackService {
	
	String handleCallback(Map<String,String> params);
	
}
