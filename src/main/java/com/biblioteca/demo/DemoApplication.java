package com.biblioteca.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.biblioteca.demo",exclude = {RedisRepositoriesAutoConfiguration.class})
@EnableCaching // <-- Habilita o suporte a cache do Spring
public class DemoApplication { 

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
