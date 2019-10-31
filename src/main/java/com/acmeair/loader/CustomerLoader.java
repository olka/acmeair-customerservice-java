/*******************************************************************************
* Copyright (c) 2013 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.acmeair.loader;

import com.acmeair.service.CustomerService;
import com.mongodb.connection.ConnectionDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CustomerLoader {

	@Inject
	CustomerService customerService;

	@Inject
	@ConfigProperty(name = "NUM_CUSTOMERS_TO_LOAD", defaultValue = "10000")
	private Integer numCustomersToLoad;

	private static Logger logger = Logger.getLogger(CustomerLoader.class.getName());

	/**
	 * Get default number of customers.
	 */

	public String queryLoader() {

		return numCustomersToLoad.toString();
	}

	private Document address = Document.parse("{streetAddress1 : \"123 Main St.\", streetAddress2 :null, city: "
			+ "\"Anytown\", stateProvince: \"NC\", country: \"USA\", postalCode: \"27617\"}");

	private String phone = "919-123-4567";
	private String business = "BUSINESS";

	/**
	 * Load customer db.
	 */
	public String loadCustomerDb(int numCustomers) {
		int batchSize = ConnectionDescription.getDefaultMaxWriteBatchSize(); 
		List<Document> documents = new ArrayList<Document>(batchSize);
		double length = 0;		
		try {
			logger.info("Start loading " + numCustomers + " customers");
			long start = System.currentTimeMillis();
			customerService.dropCustomers();		

			for (int i = 0; i < numCustomers; i++) {		
				documents.add(customerService.createCustomer("uid" + i + "@email.com", "password", "GOLD", 0, 0, phone,
						business, address));
				if ( i % batchSize == 0 ) {					
					customerService.persistCustomers(documents);		
					documents.clear();
				}
			}		
			if(!documents.isEmpty())customerService.persistCustomers(documents);		
			long stop = System.currentTimeMillis();
			logger.info("Finished loading in " + (stop - start) / 1000.0 + " seconds");
			length = (stop - start) / 1000.0;
		} catch (Exception e) {
			e.printStackTrace();			
		}

		return "Loaded " + numCustomers + " customers in " + length + " seconds";
	}
}