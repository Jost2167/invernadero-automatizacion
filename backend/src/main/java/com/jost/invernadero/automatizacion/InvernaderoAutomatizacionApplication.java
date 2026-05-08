package com.jost.invernadero.automatizacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InvernaderoAutomatizacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvernaderoAutomatizacionApplication.class, args);
	}

}
