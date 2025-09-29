package org.esupportail.nfctag.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.io.StringWriter;

/*
    Global controller advice to
    * handle uncaught exceptions
    * and init binder settings (disable request headers injection in model attributes).
 */
@ControllerAdvice
public class EsupNfcControllerAdvice implements Serializable {
	
	@Serial
    private static final long serialVersionUID = 1L;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        log.error("Erreur non gérée", ex);
        model.addAttribute("exception", ex);
        model.addAttribute("stacktrace", getStackTrace(ex));
        return "templates/uncaughtException";
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @InitBinder
    public void initBinder(ExtendedServletRequestDataBinder binder) {
        // disable binding of all fields from request headers
        // without this, headers form shibboleth authentication can be bound to model attributes
        binder.setHeaderPredicate(header -> false);
    }

}
