package org.esupportail.nfctag.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.domain.TagLog;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Service
public class StatsService {

	String[] backgroundColor = { "#FF5C4D", "#FF9933", "#FFD134",
				"#FFFF4F", "#FFFF67", "#E3FF3C", "#51FF62",
				"#5ADBFF", "#5A9FFF", "#5A9FFF", "#D759FF",
				"#FF5AC1", "#A880FF", "#F7F7F7", "#CCCCCC",
				"#B2B2B2", "#4D4D4D", "#A45077", "#FDCA59",
				"#E64D4D", "#985972" };
	
	String[] hoverBackgroundColor = { "#FF291A", "#FF6600", "#FF9E01",
   				"#FCD202", "#F8FF01", "#B0DE09", "#04D215",
   				"#0D8ECF", "#0D52D1", "#2A0CD0", "#8A0CCF",
   				"#CD0D74", "#754DEB", "#DDDDDD", "#999999",
   				"#333333", "#000000", "#57032A", "#CA9726",
   				"#990000", "#4B0C25" };
	

	public List<Object[]> nbTagLastHour(String annee) {
		
		List<Object[]> result = new ArrayList<Object[]>();


		Calendar cal = Calendar.getInstance(); // creates calendar
	    cal.setTime(new Date()); // sets calendar time/date
	    cal.add(Calendar.HOUR_OF_DAY, -1); // adds one hour
	    cal.getTime();
	    
		Long nbTagLastHour = TagLog.countFindTagLogsByAuthDateBetween(cal.getTime(), new Date());
		
		
		EntityManager em = Device.entityManager();
		Query q = em.createNativeQuery(
				"SELECT count(id) as value FROM tag_log GROUP By date_trunc('hour', auth_date) ORDER BY value DESC LIMIT 1;");
		
		Object[] now = new Object[2];
		now[0] = "Now";
		now[1] = nbTagLastHour;
		
		result.add(now);
		
		Object[] total = new Object[2];
		total[0] = "Totale";
		total[1] = q.getResultList().get(0);
		result.add(total);
		
		return result;
	}
		
		
	public List<Object[]> countNumberDeviceByUserAgent(String annee) {
		EntityManager em = Device.entityManager();
		Query q = em.createNativeQuery(
				"SELECT left(user_agent, 42) as labels, count(id) as value FROM device GROUP BY user_agent ORDER BY value DESC");
		return q.getResultList();
	}

	public List<Object[]> countNumberTagByLocation(String annee) {
		EntityManager em = TagLog.entityManager();
		Query q = em.createNativeQuery(
				"SELECT location as labels, count(id) as value FROM tag_log WHERE status='valid' GROUP BY location ORDER BY value DESC");
		return q.getResultList();
	}
	
	public List<Object[]> countNumberTagByWeek(String annee) {
		
		List<Object[]> result = new ArrayList<Object[]>();
		
		EntityManager em = TagLog.entityManager();

		Query nbWeekQuery = em.createNativeQuery(
				"SELECT count(d.date) FROM (SELECT date_part('month', auth_date) as date FROM tag_log GROUP BY date ORDER BY date) AS d;");
		
		Query q = em.createNativeQuery(
				"SELECT max(ok.realdate) AS labels, ok.application_name AS label, count(tg.id) AS count FROM tag_log as tg RIGHT JOIN (SELECT id FROM tag_log WHERE status='valid') AS val ON tg.id=val.id RIGHT JOIN (	SELECT 	max(d.realdate) as realdate, 		d.date as date, 		l.application_name as application_name 		FROM tag_log AS t, (SELECT max(auth_date) as realdate, date_part('month', auth_date) as date FROM tag_log GROUP BY date ORDER BY date) AS d, 	(SELECT application_name FROM tag_log GROUP BY application_name) AS l 		GROUP BY d.date, l.application_name ORDER BY date, application_name ASC) 	AS ok ON ok.date = date_part('month', tg.auth_date) AND ok.application_name = tg.application_name GROUP BY ok.date, label ORDER BY label, labels;");
		
		int nbWeek = Integer.valueOf(nbWeekQuery.getSingleResult().toString());
		
		List<Object[]> qResult = q.getResultList();
		System.err.println(qResult);
		int count = 0;
		for (Object object[] : qResult) {
			result.add(object);
			count++;
			if(count==nbWeek){
				Object[] saut = {null, null, null};
				
				result.add(saut);
				count = 0;
			}

		}
		
		return result;
	}	

	
	public String getnbTagLastHour(String annee) throws JsonProcessingException {

		String[] backgroundColor = { "#FF0000", "#FFFFFF" };
		
		return toChartJson(nbTagLastHour(annee), TypeChart.pie, backgroundColor, null);
	}
	
	public String getNumberTagByLocation(String annee) throws JsonProcessingException {
		
		return toChartJson(countNumberTagByLocation(annee), TypeChart.pie, backgroundColor, hoverBackgroundColor);
	}
	
	public String getNumberDeviceByUserAgent(String annee) throws JsonProcessingException {
		
		return toChartJson(countNumberDeviceByUserAgent(annee), TypeChart.pie, backgroundColor, hoverBackgroundColor);
	}

	public String getNumberTagByWeek(String annee) throws JsonProcessingException {

		String[] backgroundColor = { 
				"rgba(255, 84, 13, 0.5)", 
				"rgba(232, 12, 122, 0.5)", 
				"rgb(255, 0, 0, 0.5)", 
				"rgba(79, 120, 242, 0.5)", 
				"rgba(131, 191, 3, 0.5)", 
				"rgba(242, 182, 4, 0.5)",
				"rgba(255, 84, 13, 0.5)", 
				"rgba(232, 12, 122, 0.5)", 
				"rgb(255, 0, 0, 0.5)", 
				"rgba(79, 120, 242, 0.5)", 
				"rgba(131, 191, 3, 0.5)", 
				"rgba(242, 182, 4, 0.5)"				
				};
		
		return toChartJson(countNumberTagByWeek(annee), TypeChart.line, backgroundColor, null);
	}
	
	public String toChartJson(List<Object[]> results, TypeChart type, String[] backgroundColor, String[] hoverBackgroundColor) throws JsonProcessingException{
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonOut = null;
		ChartJson chartJson = new ChartJson();
		ChartData chartData = null;
		switch (type) {
		case line:
			chartData = new ChartDataLine();
			break;
		case pie:
			chartData = new ChartDataPie();
			break;

		default:
			break;
		}
		
		boolean sautOk=false;
		boolean next=true;
		int colorCount=0;
		for (Object[] object : results) {
			
				if (object.length>2) {
					if(object[0]==null){
						colorCount++;
						chartJson.datasets.add(chartData);
						sautOk=true;
						next=true;
						continue;
					}
					if(!sautOk){
						boolean isDate = false;
			            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			            df.setLenient(false);
			            try {
							df.parse(object[0].toString());
							isDate=true;
							
						} catch (ParseException e) {
						}
			            if(isDate){
		                	chartJson.labels.add(StringUtils.capitalize(new SimpleDateFormat("MMMM").format(df.getCalendar().getTime())));
			            }else{
							chartJson.labels.add(object[0].toString());		            	
			            }
					}
					if(next) {
						chartData = new ChartDataLine();
						chartData.backgroundColor=backgroundColor;
						chartData.label.add(object[1].toString());
						next=false;
					}
					chartData.data.add(object[2].toString());
					
				}else{
					chartJson.labels.add(object[0].toString());
					chartData.data.add(object[1].toString());
				}
			
		}
		if(!sautOk) chartJson.datasets.add(chartData);		
		
		chartData.backgroundColor=backgroundColor;
		chartData.hoverBackgroundColor=hoverBackgroundColor;
		
		jsonOut = ow.writeValueAsString(chartJson);
		return jsonOut;
	}

	class ChartJson {
		public List<String> labels = new ArrayList<String>();
		public List<ChartData> datasets = new ArrayList<ChartData>();
	}
	
	class ChartData {
		public List<String> label = new ArrayList<String>();
		public List<String> data = new ArrayList<String>();
		public String[] backgroundColor;
		public String[] hoverBackgroundColor;
		
	}
	
	class ChartDataPie extends ChartData {

   		


	}	
	
	public class ChartDataLine extends ChartData {
   		public Boolean fill = true;
		public String borderColor= "rgba(100,100,100,1)";
		public String pointBorderColor= "rgba(0,0,0,1)";
		public String pointBackgroundColor= "#fff";
		public int pointBorderWidth= 1;
		public int pointHoverRadius= 10;
		public String pointHoverBackgroundColor= "rgba(220,0,0,1)";
		public String pointHoverBorderColor= "rgba(100,100,100,1)";
		public int pointHoverBorderWidth= 2;
	}
	
	public enum TypeChart{
		line, pie
	}
}
