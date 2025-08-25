package org.esupportail.nfctag.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

/*
	En ajoutant dans security.properties devRemoteUser=toto@univ-ville.fr
	l'authentification de toto@univ-ville.fr est faite de manière transparente pour toute connexion sur esup-nfc-tag-server
	Cela permet de tester/développer sur esup-nfc-tag-server sans mettre en place un environnement SP Shibboleth
 */
@Service
public class DevClientRequestFilter extends GenericFilterBean {

	@Value("${devRemoteUser:}")
	String devRemoteUser;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {
			@Override
			public String getHeader(String name) {
				if ("REMOTE_USER".equals(name) && !StringUtils.isEmpty(devRemoteUser)) {
					return devRemoteUser;
				}
				return super.getHeader(name);
			}
		};
		chain.doFilter(wrapper, response);

	}

}
