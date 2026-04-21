package com.junseok.aicodereview;

import com.junseok.aicodereview.harness.HarnessProperties;
import com.junseok.aicodereview.provider.GitProviderProperties;
import com.junseok.aicodereview.review.ReviewProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		GitProviderProperties.class,
		HarnessProperties.class,
		ReviewProperties.class
})
public class AicodereviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(AicodereviewApplication.class, args);
	}

}
