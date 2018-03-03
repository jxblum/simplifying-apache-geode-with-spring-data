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

package example.app.server.function;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.stereotype.Component;

import example.app.client.model.Customer;

/**
 * The IdentityFunctions class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Component
@SuppressWarnings("unused")
public class IdentityFunctions {

  private final AtomicLong idSequence = new AtomicLong(System.currentTimeMillis());

  @GemfireFunction(hasResult = true)
  public Customer identify(Customer customer) {
    return customer.identifiedBy(idSequence.incrementAndGet());
  }
}
