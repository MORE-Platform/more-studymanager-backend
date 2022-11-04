package io.redlink.more.studymanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;

@SpringBootApplication(
		exclude = {
				ElasticsearchRestClientAutoConfiguration.class
		}
)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
