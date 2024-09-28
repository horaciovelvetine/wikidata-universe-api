package edu.velv.wikidata_universe_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import edu.velv.wikidata_universe_api.services.EntDocProc;
import edu.velv.wikidata_universe_api.services.FR3DConfig;
import edu.velv.wikidata_universe_api.services.FetchBroker;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

@SpringBootApplication
public class WikidataUniverseApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WikidataUniverseApiApplication.class, args);
	}

	@Bean
	public WikidataServiceManager wikidataServiceManager() {
		return new WikidataServiceManager();
	}

	@Bean
	public EntDocProc entDocProc() {
		return new EntDocProc();
	}

	@Bean
	public FetchBroker fetchBroker() {
		return new FetchBroker();
	}

	@Bean
	public FR3DConfig fr3dConfig() {
		return new FR3DConfig();
	}
}
