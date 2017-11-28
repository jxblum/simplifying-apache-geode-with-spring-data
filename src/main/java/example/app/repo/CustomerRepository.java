package example.app.repo;

import org.springframework.data.gemfire.repository.query.annotation.Trace;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import example.app.model.Customer;

/**
 * Spring Data basic CRUD and simple (OQL) Query {@link Repository} for {@link Customer Customers}.
 *
 * @author John Blum
 * @see example.app.model.Customer
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.data.repository.Repository
 * @since 1.0.0
 */
public interface CustomerRepository extends CrudRepository<Customer, Long> {

	@Trace
	Customer findByNameLike(String nameWildcard);

}
