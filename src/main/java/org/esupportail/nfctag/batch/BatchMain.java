package org.esupportail.nfctag.batch;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BatchMain {

	public static void main(String[] args) throws IOException, SQLException  {
		ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext*.xml");
		
		if(args.length < 1 || !"dbupgrade".equals(args[0])) {
			System.err.println("#####\n" +
					"Merci de préciser les arguments.\n" +
					"Voici les possibilités : \n" +
					"\t* mvn exec:java -Dexec.args=\"dbupgrade\"\n" +
					"#####");
		} else {
			if("dbupgrade".equals(args[0])) {
				DbToolService dbToolService = springContext.getBean("dbToolService", DbToolService.class);
				dbToolService.upgrade();					
			} else {
				System.err.println("Commande non trouvée.");
			}
			
			springContext.close();
		}
	}

}
