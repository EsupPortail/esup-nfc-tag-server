package org.esupportail.nfctag.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletExceptionHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		final Throwable e = (Throwable) request.getAttribute("javax.servlet.error.exception");
		log.error("Servlet Caught unhandled exception: " + e, e);

		RequestDispatcher rd = request.getRequestDispatcher("/uncaughtException");
		rd.forward(request, response);
	}
}
