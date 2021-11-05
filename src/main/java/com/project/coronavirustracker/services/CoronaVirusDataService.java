package com.project.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.coronavirustracker.models.LocationStats;

// Making this as a spring service
@Service
public class CoronaVirusDataService {
	private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	private List<LocationStats> allStats = new ArrayList<>();
	
	public List<LocationStats> getAllStats() {
		return allStats;
	}


	// After running the app the below class will be executed
	@PostConstruct
	// Schedules run of a method based on daily bases.
	// Run this method for every second
	// @Scheduled(cron = "* * * * * *")
	public void fetchVirusData() throws IOException, InterruptedException {
		List<LocationStats> newStats = new ArrayList<>();
		HttpClient client = HttpClient.newHttpClient();
		
		// Creating a request to the URL using Builder() pattern.
		HttpRequest request = HttpRequest.newBuilder()
							.uri(URI.create(VIRUS_DATA_URL))
							.build();
		HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
		//System.out.println(httpResponse.body());
		
		StringReader csvBodyReader = new StringReader(httpResponse.body());

		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			LocationStats locationStat = new LocationStats();
			locationStat.setState(record.get("Province/State"));
			locationStat.setCountry(record.get("Country/Region"));
			int latestCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			locationStat.setLatestTotalCases(latestCases);
			locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
			//System.out.println(locationStat);
			newStats.add(locationStat);
		}
		this.allStats = newStats;
	}
}
