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

package example.app.client.repo;

import org.springframework.data.gemfire.repository.query.annotation.Trace;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import example.app.client.model.Customer;

/**
 * Spring Data {@link Repository} with basic CRUD and simple (OQL) Query data access operations
 * for {@link Customer Customers}.
 *
 * A Spring Data {@link Repository} is analogous to a Data Access Object (DAO).
 *
 * @author John Blum
 * @see example.app.client.model.Customer
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.data.repository.Repository
 * @since 1.0.0
 */
public interface CustomerRepository extends CrudRepository<Customer, Long> {

	@Trace
	Customer findByNameLike(String nameWildcard);

}
