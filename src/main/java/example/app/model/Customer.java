package example.app.model;

import org.springframework.data.gemfire.mapping.annotation.Indexed;
import org.springframework.data.gemfire.mapping.annotation.Region;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * {@link Customer} is an Abstract Data Type (ADT) modeling a customer.
 *
 * @author John Blum
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @see org.springframework.data.gemfire.mapping.annotation.Indexed
 * @since 1.0.0
 */
@Data
@Region("Customers")
@RequiredArgsConstructor(staticName = "newCustomer")
public class Customer {

	@NonNull
	private Long id;

	@NonNull
	@Indexed(from = "/Customers")
	private String name;

}
