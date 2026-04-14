package core.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.gson.JsonObject;

@Component
public class LoginInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		HttpSession session = request.getSession(false);

		Object member = (session != null) ? session.getAttribute("member") : null;
		Object admin = (session != null) ? session.getAttribute("admin") : null;

		// 會員未登入
		if (member == null && admin == null) {
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			JsonObject object = new JsonObject();
			object.addProperty("success", false);
			object.addProperty("message", "尚未登入，請先登入!");
			response.getWriter().write(object.toString());
			return false;
		}

		return true;
	}
}
