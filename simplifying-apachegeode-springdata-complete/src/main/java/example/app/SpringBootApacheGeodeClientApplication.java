/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app;

import static example.app.util.Assertions.assertCustomer;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.query.Index;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableIndexing;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.repository.CrudRepository;

import example.app.model.Customer;
import example.app.repo.CustomerRepository;

/**
 * The {@link SpringBootApacheGeodeClientApplication} class is a simple Spring Boot, Apache Geode
 * cache client application enabled with Spring Data Geode to store and query data in Apache Geode.
 *
 * The purpose of this application is to demonstrate that no boilerplate code (i.e. plumbing) is required at all
 * to build a simple, CRUD based application using Apache Geode, making it very easy and simple to get started.
 *
 * While this application starts from a local context, as a {@link ClientCache}, it is very simple to make use
 * of Apache Geode's client/server topology by removing the {@literal clientRegionShortcut} configuration setting
 * declared in the {@link EnableEntityDefinedRegions} annotation.  By removing the {@link ClientRegionShortcut#LOCAL}
 * setting, the client will send all Region data access operations to a server.  By default, the client
 * {@literal "Customers"} {@link Region} is a {@literal PROXY} and will use the {@link ClientCache ClientCache's}
 * {@literal DEFAULT} {@link Pool}, which Apache Geode configures automatically to connect to a server
 * running on {@literal localhost}, listening on port {@literal 40404}.
 *
 * Since the data will be "serialized" when sent to the server, this application also enables Apache Geode's PDX
 * serialization functionality simply by declaring the {@link EnablePdx} annotation.
 *
 * I have also configured an Apache Geode OQL {@literal HASH} {@link Index} on the
 * {@link Customer#getName() Customer's name} field.  This is useful since we query for a {@link Customer} by name
 * using a OQL wildcard.
 *
 * All data access operations (CRUD + OQL Queries) are performed using SDG's Repository infrastructure.
 * Simply define a {@link CrudRepository} interface extension for {@link Customer} along with any application-specific
 * OQL queries, and you are up and running.
 *
 * Finally, while I provide Gfsh scripts in the ${project.home}/etc/ directory, which configures a complete
 * Apache Geode cluster, it is possible just to setup a empty cluster (i.e. No Regions, No Indexes, No Anything)
 * and uncomment the {@link EnableClusterConfiguration} annotation below and SDG will configure the server(s)
 * in the cluster for you!  How cool is that!
 *
 * However, you must have a full installation of Apache Geode installed on your machine for this to work.
 * This SDG feature makes use of several internal Apache Geode features.
 *
 * Happy coding!!
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see org.springframework.data.gemfire.config.annotation.EnableIndexing
 * @see org.springframework.data.gemfire.config.annotation.EnablePdx
 * @see org.springframework.data.gemfire.repository.config.EnableGemfireRepositories
 * @since 1.0.0
 */
@SpringBootApplication
@ClientCacheApplication
@EnableEntityDefinedRegions(basePackageClasses = Customer.class, clientRegionShortcut = ClientRegionShortcut.LOCAL)
@EnableGemfireRepositories(basePackageClasses = CustomerRepository.class)
@EnableIndexing
//@EnablePdx
//@EnableClusterConfiguration(useHttp = true)
public class SpringBootApacheGeodeClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootApacheGeodeClientApplication.class, args);
	}

	@Bean
	@SuppressWarnings("unused")
	ApplicationRunner runner(CustomerRepository customerRepository) {

		return args -> {

			assertThat(customerRepository.count()).isEqualTo(0);

			Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

			System.err.printf("Saving Customer [%s]...%n", jonDoe);

			jonDoe = customerRepository.save(jonDoe);

			assertCustomer(jonDoe, 1L, "Jon Doe");
			assertThat(customerRepository.count()).isEqualTo(1);

			System.err.println("Querying for Customer [SELECT * FROM /Customers WHERE name LIKE '%Doe']...");

			Customer queriedJonDoe = customerRepository.findByNameLike("%Doe");

			assertThat(queriedJonDoe).isEqualTo(jonDoe);

			System.err.printf("Customer was [%s]%n", queriedJonDoe);
		};
	}
}
