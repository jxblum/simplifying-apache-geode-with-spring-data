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

package example.app.client.util;

import static org.assertj.core.api.Assertions.assertThat;

import example.app.client.model.Customer;

/**
 * Abstract utility class with assertions for {@link Customer} data.
 *
 * @author John Blum
 * @see org.assertj.core.api.Assertions
 * @since 1.0.0
 */
public abstract class Assertions {

	public static void assertCustomer(Customer customer, Long expectedId, String expectedName) {
		assertThat(customer).isNotNull();
		assertThat(customer.getId()).isEqualTo(expectedId);
		assertThat(customer.getName()).isEqualTo(expectedName);
	}
}
