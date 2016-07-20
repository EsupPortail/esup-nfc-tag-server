package org.esupportail.nfctag.web.manager;

import org.esupportail.nfctag.domain.TagLog;
import org.springframework.roo.addon.web.mvc.controller.finder.RooWebFinder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/manager/taglogs")
@Controller
@RooWebScaffold(path = "manager/taglogs", formBackingObject = TagLog.class, create=false, delete=false, update=false, exposeFinders=false)
@RooWebFinder
public class TagLogController {

    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if(sortFieldName == null){
    		sortFieldName = "authDate";
    		sortOrder = "DESC";
    	}
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("taglogs", TagLog.findTagLogEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) TagLog.countTagLogs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("taglogs", TagLog.findAllTagLogs(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "manager/taglogs/list";
    }
    
}
