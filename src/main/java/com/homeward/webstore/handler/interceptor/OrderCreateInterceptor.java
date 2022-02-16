package com.homeward.webstore.handler.interceptor;

import com.homeward.webstore.common.utils.JwtUtil;
import com.homeward.webstore.mapper.AuthenticationMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class OrderCreateInterceptor implements HandlerInterceptor {
    private final AuthenticationMapper authenticationMapper;

    public OrderCreateInterceptor(AuthenticationMapper authenticationMapper) {
        this.authenticationMapper = authenticationMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uuid = JwtUtil.getUserId();
        Cookie[] cookieArray = request.getCookies();
        if (cookieArray == null) {
            return true;
        }

        List<Integer> itemIdList = authenticationMapper.isSingleCart(uuid);

        String requestURI = request.getRequestURI();
        String[] split = requestURI.split("/");
        String idString = split[split.length - 1];
        Integer itemId = Integer.valueOf(idString);

        String cookieName = uuid + itemId;

        for (Cookie cookie : cookieArray) {
            if (Objects.equals(cookieName, cookie.getName()) && itemIdList.contains(itemId)) {
                log.error(uuid + " have duplicated cart");
                throw new RuntimeException("duplicated cart found");
            }
        }

        return true;
    }
}
