package example.app.util;

import static org.assertj.core.api.Assertions.assertThat;

import example.app.model.Customer;

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
