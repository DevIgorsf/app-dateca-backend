package com.dat.dateca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DatecaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatecaApplication.class, args);
	}

}
