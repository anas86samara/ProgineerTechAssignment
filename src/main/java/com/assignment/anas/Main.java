package com.assignment.anas;

import io.vertx.core.Vertx;

/**
 * Main method to run the Vertx services
 * @author Anas
 *
 */
public class Main {

	public static void main(String[] args) {
		
		 Vertx vertx = Vertx.vertx();

	     vertx.deployVerticle(new MyServicesVerticle());
	}
}
