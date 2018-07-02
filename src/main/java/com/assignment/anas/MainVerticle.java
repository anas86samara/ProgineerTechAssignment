package com.assignment.anas;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

/**
 * @author Anas
 *
 */
public class MainVerticle extends AbstractVerticle {


	/**
	 * For google API (Places API)
	 */
	private final static String API_KEY = "AIzaSyBlOdRoFaCHZ-F4xBuFEL2ER-AQbUHYyCA";
	private final static String URL = "maps.googleapis.com";

	/**
	 * Resistant storage to record requested countries
	 */
	private static final String REQUESTS_FILE = "requestsHistory.txt";

	private BufferedWriter requestsHistory;

	/* (non-Javadoc)
	 * @see io.vertx.core.AbstractVerticle#start(io.vertx.core.Future)
	 */
	@Override
	public void start(Future<Void> fut) throws Exception {

		requestsHistory = new BufferedWriter(new FileWriter(REQUESTS_FILE, true));

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);

		// Route for Metrics resource
		Route metricRoute = router.route("/metrics/").handler(routingContext -> {

			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/plain");
			response.setChunked(true);
			response.write("METRIC RESOURCES\n\n");
			try {
				response.write(getMetrics());
			} catch (IOException e) {
				e.printStackTrace();
			}
			routingContext.response().end();
		});

		// Route for Places resource
		Route placesRoute = router.route("/places/").handler(routingContext -> {
			HttpServerRequest request = routingContext.request();
			String input = request.getParam("input");
			String country = request.getParam("country");

			// record the requested country
			try {
				requestsHistory.write(country+"\n");
				requestsHistory.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			WebClient client = WebClient.create(vertx);

			// Send a GET request
			client.get(443, URL, "/maps/api/place/autocomplete/json")
			.addQueryParam("input", input)
			.addQueryParam("country", country)
			.addQueryParam("key", API_KEY)
			.ssl(true)
			.send(ar -> {
				String responseString;
				if (ar.succeeded()) {
					HttpResponse<Buffer> response = ar.result();
					System.out.println("Received response from googleAPI with status code " + response.statusCode());
					JsonObject responseJson = response.bodyAsJsonObject();
					responseString = formatResult(responseJson);
				} else {
					responseString = "ERROR " + ("Something went wrong whilst requesting googleAPI " + ar.cause().getMessage());;
					System.out.println(responseString);
				}

				HttpServerResponse response = routingContext.response();
				response.putHeader("content-type", "text/plain");
				response.setChunked(true);
				response.write("PLACES RESOURCE: user parameters are \n'" + input + "'  and  '" + country + "'\n\n");
				response.write(responseString);

				routingContext.response().end();
			});
		});
		server.requestHandler(router::accept).listen(8080);
	}

	/**
	 * Read recorded records for countries and 
	 * @return formatted string of the countries associated with number of times requested
	 * @throws IOException
	 */
	private String getMetrics() throws IOException {
		int totalNO = 0;
		Map<String,Integer> countriesMap = new HashMap<String,Integer>();
		FileReader fileReader = new FileReader(REQUESTS_FILE);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String sCurrentLine;
		while ((sCurrentLine = bufferedReader.readLine()) != null) {
			++totalNO;
			Integer requestsNumber = countriesMap.get(sCurrentLine);
			if(requestsNumber == null) {
				countriesMap.put(sCurrentLine, 1);
			}else {
				countriesMap.put(sCurrentLine, ++requestsNumber);
			}
		}
		return countriesMap.toString() + "\n TOTAL NUMBER OF REQUESTS: " + totalNO;
	}

	/**
	 * Parse places JSON returned from GOOGLE API 
	 * @param json
	 * @return the descriptions of places only
	 */
	private String formatResult(JsonObject json) {
		StringBuilder finalString = new StringBuilder();
		List<String> descriptions = new ArrayList<String>();

		JsonArray predictions = json.getJsonArray("predictions");
		if (predictions != null && predictions.size() > 0) {
			predictions.forEach(object -> {
				if (object instanceof JsonObject) {
					JsonObject prediction = (JsonObject) object;
					descriptions.add(prediction.getString("description"));
				}
			});
		}
		for (int i = 0; i < descriptions.size() && i < 5; i++) {
			finalString.append(descriptions.get(i)+"\n");
		}
		return finalString.toString();
	}
}
