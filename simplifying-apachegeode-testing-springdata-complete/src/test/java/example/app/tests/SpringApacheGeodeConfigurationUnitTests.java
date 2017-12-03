/*
 *  Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package example.app.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.annotation.Resource;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.query.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.IndexType;
import org.springframework.data.gemfire.mapping.MappingPdxSerializer;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.client.model.Customer;
import example.app.client.repo.CustomerRepository;
import example.app.config.SpringApacheGeodeConfiguration;

/**
 * Unit tests for {@link SpringApacheGeodeConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class SpringApacheGeodeConfigurationUnitTests {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private GemFireCache gemfireCache;

	@Resource(name = "Customers")
	private Region<Long, Customer> customers;

	@Test
	public void gemfireCacheIsAClientCache() {
		assertThat(this.gemfireCache).isInstanceOf(ClientCache.class);
	}

	@Test
	public void gemfireMemberNameIsCorrect() {
		assertThat(this.gemfireCache.getDistributedSystem().getProperties().get("name"))
			.isEqualTo("SpringApacheGeodeConfiguration");
	}

	@Test
	public void cachePdxSerializationConfigurationIsCorrect() {
		assertThat(this.gemfireCache.getPdxSerializer()).isInstanceOf(MappingPdxSerializer.class);
	}

	@Test
	public void customersRegionExistsAndConfigurationIsCorrect() {

		assertThat(this.customers).isNotNull();
		assertThat(this.customers).isSameAs(this.gemfireCache.getRegion("/Customers"));
		assertThat(this.customers.getRegionService()).isEqualTo(this.gemfireCache);
		assertThat(this.customers.getName()).isEqualTo("Customers");
		assertThat(this.customers.getFullPath()).isEqualTo("/Customers");
		assertThat(this.customers.getAttributes()).isNotNull();
		assertThat(this.customers.getAttributes().getDataPolicy()).isEqualTo(DataPolicy.EMPTY);

		assertCustomersNameIndexExists(customers);
	}

	private void assertCustomersNameIndexExists(Region<Long, Customer> customers) {

		Index customersNameHashIdx = customers.getRegionService().getQueryService().getIndex(customers,
			"CustomersNameHashIdx");

		assertThat(customersNameHashIdx).isNotNull();
		assertThat(customersNameHashIdx.getName()).isEqualTo("CustomersNameHashIdx");
		assertThat(customersNameHashIdx.getIndexedExpression()).isEqualTo("name");
		assertThat(customersNameHashIdx.getFromClause()).isEqualTo("/Customers");
		assertThat(customersNameHashIdx.getType()).isEqualTo(IndexType.HASH.getGemfireIndexType());
	}

	@Test
	public void saveAndLoadCustomer() {

		Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

		jonDoe = this.customerRepository.save(jonDoe);

		assertThat(jonDoe).isNotNull();
		assertThat(jonDoe.getId()).isEqualTo(1L);
		assertThat(jonDoe.getName()).isEqualTo("Jon Doe");

		Customer loadedJonDoe = this.customerRepository.findById(jonDoe.getId()).orElse(null);

		assertThat(loadedJonDoe).isEqualTo(jonDoe);

		verify(this.customers, times(1)).put(eq(1L), eq(jonDoe));
		verify(this.customers, times(1)).get(eq(1L));
	}

	@Configuration
	@EnableGemFireMockObjects
	@Import(SpringApacheGeodeConfiguration.class)
	static class TestConfiguration {
	}
}
