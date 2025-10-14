package org.esupportail.nfctag.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.dao.DeviceDao;
import org.esupportail.nfctag.dao.TagLogDao;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class StatsService {

	enum ChartType { line, bar};

	@PersistenceContext
	private EntityManager entityManager;

	@Resource
	private DeviceDao deviceDao;

	@Resource
	private TagLogDao tagLogDao;

	String[] backgroundColor = { 
			"rgba(230, 25, 75, 0.5)", 
			"rgba(60, 180, 75, 0.5)", 
			"rgba(255, 225, 25, 0.5)", 
			"rgba(0, 130, 200, 0.5)", 
			"rgba(245, 130, 48, 0.5)",
			"rgba(145, 30, 180, 0.5)", 
			"rgba(70, 240, 240, 0.5)", 
			"rgba(240, 50, 230, 0.5)", 
			"rgba(210, 245, 60, 0.5)", 
			"rgba(250, 190, 190, 0.5)",
			"rgba(0, 128, 128, 0.5)",
			"rgba(170, 110, 40, 0.5)",
			"rgba(255, 250, 200, 0.5)",
			"rgba(128, 0, 0, 0.5)",
			"rgba(170, 255, 195, 0.5)"
			};
	
	String[] hoverBackgroundColor = { 
			"rgba(230, 25, 75, 1)", 
			"rgba(60, 180, 75, 1)", 
			"rgba(255, 225, 25, 1)", 
			"rgba(0, 130, 200, 1)", 
			"rgba(245, 130, 48, 1)",
			"rgba(145, 30, 180, 1)", 
			"rgba(70, 240, 240, 1)", 
			"rgba(240, 50, 230, 1)", 
			"rgba(210, 245, 60, 1)", 
			"rgba(250, 190, 190, 1)",
			"rgba(0, 128, 128, 1)",
			"rgba(170, 110, 40, 1)",
			"rgba(255, 250, 200, 1)",
			"rgba(128, 0, 0, 1)",
			"rgba(170, 255, 195, 1)"
			};
	

	public List<Object[]> nbTagThisDay(String annee) {
		
		List<Object[]> result = new ArrayList<Object[]>();

		Long nbTagThisDay = tagLogDao.countFindTagLogsThisDay();
		
		EntityManager em = entityManager;
		Query q = em.createNativeQuery(
				"SELECT count(id) as value, date_trunc('day', auth_date) FROM tag_log WHERE date_part('year', auth_date) = " + annee + " GROUP By date_trunc('day', auth_date) ORDER BY value DESC LIMIT 1;");
		Long max = Long.valueOf(0);
        LocalDateTime maxDay = null;
		if(q.getResultList().size()>0){
			Object[] nbAndDay = (Object[])q.getResultList().get(0);
			max = (Long)nbAndDay[0];
			maxDay = ((LocalDateTime )nbAndDay[1]);
		}
		if (nbTagThisDay.doubleValue() > max.doubleValue()) max = nbTagThisDay.longValue();
		Double percentNow = (nbTagThisDay.doubleValue() / max.doubleValue()) * 100;
		
		Object[] now = new Object[2];
		now[0] = "Today : " + nbTagThisDay;
		now[1] = percentNow.intValue();
		
		result.add(now);
		
		Object[] total = new Object[2];
		total[0] = String.format("Max in 1 day : %s (%td/%tm/%tY)", max, maxDay, maxDay, maxDay);
		total[1] = 100 - percentNow.intValue();
		result.add(total);
		
		return result;
	}
		
		
	public List<Object[]> countNumberDeviceByApplication(String annee) {
		EntityManager em = entityManager;
		Query q = em.createNativeQuery(
				"SELECT a.name as labels, count(d.id) as value FROM device as d, application as a WHERE a.id = d.application GROUP BY a.name ORDER BY value DESC");
		return q.getResultList();
	}

	public List<Object[]> countNumberTagByApplication(String annee) {
		EntityManager em = entityManager;
		Query q = em.createNativeQuery(
				"SELECT application_name, count(*) as value FROM tag_log WHERE status='valid' AND date_part('year', auth_date) = " + annee + " GROUP BY application_name ORDER BY value desc");
		return q.getResultList();
	}

	public List<String> findApplications(String annee) {
		EntityManager em = entityManager;
		Query appNamesQuery = em.createNativeQuery("SELECT application_name FROM tag_log WHERE status='valid' AND date_part('year', auth_date) = " + annee + " GROUP BY application_name ORDER BY COUNT(*) DESC;");
		return appNamesQuery.getResultList();
	}

	public List<Object[]> countNumberTagByYear() {
		List<String> years = years();

		List<Object[]> result = new ArrayList<Object[]>();

		EntityManager em = entityManager;

		Query appNamesQuery = em.createNativeQuery("SELECT application_name FROM tag_log WHERE status='valid' GROUP BY application_name ORDER BY COUNT(*) DESC;");
		List<String> appList = appNamesQuery.getResultList();

		Query q = em.createNativeQuery(
				"SELECT cast(cast(date_part('year', auth_date) as int) as varchar) as labels, application_name as label, count(*) as data FROM tag_log WHERE status='valid' GROUP BY labels, label ORDER BY label, labels");

		List<Object[]> qResult = q.getResultList();
		String app = appList.get(0);
		for (String appName : appList){
			if(!app.equals(appName)){
				Object[] saut = {null, null, null};
				result.add(saut);
				app = appName;
			}
			for (String year: years){
				Object[] objectToApp = {year, appName, 0};
				for (Object[] object : qResult) {
					if(object[0].toString().equals(year) && object[1].toString().equals(appName)){
						objectToApp = object;
					}
				}
				result.add(objectToApp);
			}
		}
		return result;
	}

	public List<String> years() {
		EntityManager em = entityManager;
		Query q = em.createNativeQuery(
				"SELECT cast(cast(date_part('year', auth_date) as int) as varchar) as year FROM tag_log WHERE status='valid' GROUP BY year ORDER BY year");
		return q.getResultList();
	}

	public List<Object[]> countNumberTagByLocation(String annee, String application) {
		EntityManager em = entityManager;
		String sqlApplication = StringUtils.isEmpty(application) ? "" : " AND application_name='" + application + "' ";
		Query q = em.createNativeQuery(
				"SELECT location as labels, count(id) as value FROM tag_log WHERE status='valid' AND date_part('year', auth_date) = " + annee + sqlApplication + " GROUP BY location ORDER BY value DESC LIMIT 25");
		return q.getResultList();
	}
	
	public List<Object[]> countNumberTagByMouth(String annee, List<String> month) {
		
		List<Object[]> result = new ArrayList<Object[]>();

		EntityManager em = entityManager;

		Query appNamesQuery = em.createNativeQuery("SELECT application_name FROM tag_log WHERE date_part('year', auth_date) = " + annee + " GROUP BY application_name ORDER BY COUNT(*) DESC;");
		
		List<String> appList = appNamesQuery.getResultList();
		
		String sql = "SELECT trim(to_char(date_part('month', auth_date), '99')) as labels, t.application_name as label, count(id) as data FROM tag_log as t RIGHT JOIN (SELECT date_part('month', auth_date) as date FROM tag_log  GROUP BY date) AS m ON m.date = date_part('month', t.auth_date) WHERE date_part('year', auth_date) = " + annee + " GROUP BY labels, label ORDER BY label, labels;";
		
		Query q = em.createNativeQuery(sql);

        List<Object[]> qResult = q.getResultList();
        if(!qResult.isEmpty()){
            String app = appList.get(0);
            for (String appName : appList) {
                if (!app.equals(appName)) {
                    Object[] saut = {null, null, null};
                    result.add(saut);
                    app = appName;
                }
                for (int i = 0; i < month.size(); i++) {
                    Object[] objectToApp = {month.get(i), appName, 0};
                    for (Object[] object : qResult) {
                        if (object[0].toString().equals(month.get(i)) && object[1].toString().equals(appName)) {
                            objectToApp = object;
                        }
                    }
                    result.add(objectToApp);
                }
            }
        }

		return result;
	}	

	
	public String getnbTagThisDay(String annee) throws JsonProcessingException {

		String[] backgroundColor = { "rgba(230, 25, 75, 0.5)", "rgba(75, 75, 75, 0.5)" };
		String[] hoverBackgroundColor = { "rgba(230, 25, 75, 1)", "rgba(75, 75, 75, 1)" };
		
		return toChartJsonSimple(nbTagThisDay(annee), backgroundColor, hoverBackgroundColor);
	}
	
	public String getNumberTagByLocation(String annee, String application) throws JsonProcessingException {
		return toChartJsonSimple(countNumberTagByLocation(annee, application),  backgroundColor, hoverBackgroundColor);
	}
	
	public String getNumberDeviceByUserAgent(String annee) throws JsonProcessingException {
		return toChartJsonSimple(countNumberDeviceByApplication(annee), backgroundColor, hoverBackgroundColor);
	}

	public String getNumberTagByApplication(String annee) throws JsonProcessingException {
		return toChartJsonSimple(countNumberTagByApplication(annee), backgroundColor, hoverBackgroundColor);
	}

	public String getNumberTagByYear() throws JsonProcessingException {
		return toChartJsonComplex(years(), countNumberTagByYear(), backgroundColor, hoverBackgroundColor, ChartType.line);
	}

	public String getNumberTagByWeek(String annee) throws JsonProcessingException {
		List<String> month = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
		return toChartJsonComplex(month, countNumberTagByMouth(annee, month), backgroundColor, hoverBackgroundColor, ChartType.bar);
	}
	
	public String toChartJsonSimple(List<Object[]> results, String[] backgroundColor, String[] hoverBackgroundColor) throws JsonProcessingException{
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonOut = null;
		ChartJsonSimple chartJson = new ChartJsonSimple();
		ChartDataPie chartData = new ChartDataPie();

		for (Object[] object : results) {
			chartJson.labels.add(object[0].toString());
			chartData.data.add(object[1].toString());
		}
		chartJson.datasets.add(chartData);		
		chartData.backgroundColor=backgroundColor;
		chartData.hoverBackgroundColor=hoverBackgroundColor;
		jsonOut = ow.writeValueAsString(chartJson);
		return jsonOut;
	}
	
	public String toChartJsonComplex(List<String> labels, List<Object[]> results, String[] backgroundColor, String[] hoverBackgroundColor, ChartType chartType) throws JsonProcessingException{
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonOut = null;
		ChartJsonComplex chartJson = new ChartJsonComplex();
		ChartDataBar chartData = new ChartDataBar();
		ChartDataLine chartData2 = new ChartDataLine();
		
		for(String label : labels){
			boolean isDate = false;
            DateFormat df = new SimpleDateFormat("MM");
            df.setLenient(false);
            try {
				df.parse(label);
				isDate=true;
				
			} catch (ParseException e) {
			}
            if(isDate){
            	chartJson.labels.add(StringUtils.capitalize(new SimpleDateFormat("MMMM").format(df.getCalendar().getTime())));
            }else{
				chartJson.labels.add(label);		            	
            }
		}
		
		boolean next=true;
		int colorCount=0;
		
		for (Object[] object : results) {
			if(object[0]==null){
				colorCount++;
				if(colorCount>=backgroundColor.length) {
					colorCount=0;
				}
				if(ChartType.bar.equals(chartType)) {
					chartJson.datasets.add(chartData);
				} else {
					chartJson.datasets.add(chartData2);
				}
				next=true;
				continue;
			}
			if(next) {
				chartData = new ChartDataBar();
				chartData.backgroundColor = backgroundColor[colorCount];
				chartData.hoverBackgroundColor = hoverBackgroundColor[colorCount];
				chartData.label = object[1].toString();
				chartData2 = new ChartDataLine();
				chartData2.borderColor = backgroundColor[colorCount];
				chartData2.backgroundColor = backgroundColor[colorCount];
				chartData2.hoverBackgroundColor = hoverBackgroundColor[colorCount];
				chartData2.label = object[1].toString();
				next=false;
			}
			if(ChartType.bar.equals(chartType)) {
				chartData.data.add(object[2].toString());
			} else {
				chartData2.data.add(object[2].toString());
			}
		}

		if(ChartType.bar.equals(chartType)) {
			chartJson.datasets.add(chartData);
		} else {
			chartJson.datasets.add(chartData2);
		}
		jsonOut = ow.writeValueAsString(chartJson);
		return jsonOut;
	}

	class ChartJsonSimple {
		public List<String> labels = new ArrayList<String>();
		public List<ChartDataPie> datasets = new ArrayList<ChartDataPie>();
	}

	class ChartJsonComplex {
		public List<String> labels = new ArrayList<String>();
		public List<ChartDataComplex> datasets = new ArrayList<ChartDataComplex>();
	}
	
	class ChartDataPie {
		public List<String> data = new ArrayList<String>();
		public String[] backgroundColor;
		public String[] hoverBackgroundColor;
	}	
	
	public class ChartDataComplex {
		
	}
	
	public class ChartDataBar extends ChartDataComplex {
		public String type = "bar";
		public String label = new String();
		public List<String> data = new ArrayList<String>();
		public String backgroundColor;
		public String hoverBackgroundColor;
	}
	
	public class ChartDataLine extends ChartDataComplex {
		public String type = "line";
		public String label = new String();
		public List<String> data = new ArrayList<String>();
		public String borderColor;
		public String backgroundColor;
		public String hoverBackgroundColor;
		public Boolean fill = true;
	}
	
	public enum TypeChart{
		line, pie
	}
}
