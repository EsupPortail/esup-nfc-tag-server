package org.esupportail.nfctag.web;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.*;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @see PageableHandlerMethodArgumentResolver
 * Resolver de Pageable qui regarde dans la session si un paramètre size a été sauvegardé.
 * Si oui, il l'utilise pour créer le Pageable.
 * Si non, il utilise la valeur par défaut.
 * Si un paramètre size est présent dans la requête, il l'utilise et le sauvegarde en session.
 */
public class SessionAwarePageableResolver extends PageableHandlerMethodArgumentResolver {

    private static final String SIZE_IN_SESSION = "paginationSize";

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        if(webRequest.getParameter("size")==null) {
            Object sessionSize = webRequest.getAttribute(SIZE_IN_SESSION, NativeWebRequest.SCOPE_SESSION);
            if (sessionSize != null) {
                int size = (int) sessionSize;
                pageable = PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
            }
        } else {
            webRequest.setAttribute(SIZE_IN_SESSION, pageable.getPageSize(), NativeWebRequest.SCOPE_SESSION);
        }
        return pageable;
    }
}