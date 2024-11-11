package edu.velv.wikidata_universe_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import edu.velv.wikidata_universe_api.services.EntDocProc;
import edu.velv.wikidata_universe_api.services.FetchBroker;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

@SpringBootApplication
public class WikidataUniverseApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WikidataUniverseApiApplication.class, args);
	}

	@Bean
	WikidataServiceManager wikidataServiceManager() {
		return new WikidataServiceManager();
	}

	@Bean
	EntDocProc entDocProc() {
		return new EntDocProc();
	}

	@Bean
	FetchBroker fetchBroker() {
		return new FetchBroker();
	}
}
