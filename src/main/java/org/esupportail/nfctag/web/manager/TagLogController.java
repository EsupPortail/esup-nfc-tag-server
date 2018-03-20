/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.nfctag.web.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.TagLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/manager/taglogs")
@Controller
@RooWebScaffold(path = "manager/taglogs", formBackingObject = TagLog.class, create=false, delete=false, update=false)
public class TagLogController {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
	List<String> listSearchBy = Arrays.asList("authDate", "applicationName", "location", "eppnInit", "numeroId", "csn", "desfireId");
	
    @RequestMapping(produces = "text/html")
    public String list(
    		@RequestParam(value = "searchString", required = false) String searchString,
    		@RequestParam(value = "applicationFilter", required = false) String applicationFilter,
    		@RequestParam(value = "statusFilter", required = false) String statusFilter,
    		@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {

    	if(sortFieldName == null){
    		sortFieldName = "authDate";
    		sortOrder = "DESC";
    	}

        int sizeNo = size == null ? 10 : size.intValue();
        final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
    	List<TagLog> taglogs = TagLog.findTagLogEntries(firstResult, sizeNo, sortFieldName, sortOrder);

    	EntityManager em = TagLog.entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<TagLog> query = criteriaBuilder.createQuery(TagLog.class);
        Root<TagLog> c = query.from(TagLog.class);
        final List<Predicate> predicates = new ArrayList<Predicate>();
        final List<Order> orders = new ArrayList<Order>();
    	
        if("DESC".equals(sortOrder.toUpperCase())){
        	orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
        }else{
        	orders.add(criteriaBuilder.asc(c.get(sortFieldName)));
        }
        
        if(applicationFilter != null && applicationFilter != ""){
        	predicates.add(criteriaBuilder.equal(c.get("applicationName"), applicationFilter));
        }else{
        	applicationFilter="";
        }

        if(statusFilter != null && statusFilter != ""){
        	predicates.add(criteriaBuilder.equal(c.get("status"), TagLog.Status.valueOf(statusFilter)));
        }else{
        	statusFilter="";
        }
        
        if(searchString!=null && searchString!=""){
	        Expression<Boolean> fullTestSearchExpression = criteriaBuilder.function("fts", Boolean.class, criteriaBuilder.literal("'"+searchString+"'"));
	        Expression<Double> fullTestSearchRanking = criteriaBuilder.function("ts_rank", Double.class, criteriaBuilder.literal("'"+searchString+"'"));
	        predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
	        orders.add(criteriaBuilder.desc(fullTestSearchRanking));
        }else{
        	searchString="";
        }
        
        orders.add(criteriaBuilder.desc(c.get(sortFieldName)));        
        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(orders);
        query.select(c);
        taglogs = em.createQuery(query).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList();
        
        float nrOfPages = (float) em.createQuery(query).getResultList().size() / sizeNo;
        
        uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        uiModel.addAttribute("applications", Application.findAllApplications());
        uiModel.addAttribute("status", TagLog.Status.values());
        uiModel.addAttribute("page", page);
        uiModel.addAttribute("size", size);
    	uiModel.addAttribute("taglogs", taglogs);
    	uiModel.addAttribute("searchString", searchString);
        uiModel.addAttribute("applicationFilter", applicationFilter);
        uiModel.addAttribute("statusFilter", statusFilter);
        uiModel.addAttribute("listSearchBy", listSearchBy);
        uiModel.addAttribute("queryUrl", "?statusFilter="+statusFilter+"&applicationFilter="+applicationFilter+"&searchString="+searchString);
        addDateTimeFormatPatterns(uiModel);
        return "manager/taglogs/list";
    }

}
