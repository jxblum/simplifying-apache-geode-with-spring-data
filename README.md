## Example Spring Boot Apache Geode Client Application

This example demonstrates the use of [_Spring Boot_](http://projects.spring.io/spring-boot/)
and [_Spring Data Geode_](http://projects.spring.io/spring-data-gemfire/)
to build a simple [_Apache Geode_](http://geode.apache.org/) client application
to persist and query `Customer` data.

The purpose of this example is to demonstrate how **easy** and **quick**
it can be **to get started** building a simple _Spring_ application
using _Apache Geode_, and scale it up.

No other data store, not _Redis_ nor _Hazelcast_, is as simple
to setup or use given the power of _Spring Data Geode_ (SDG), despite
there being [Spring Data modules](http://projects.spring.io/spring-data/)
for both [Redis](https://projects.spring.io/spring-data-redis)
and [Hazelcast](https://github.com/hazelcast/spring-data-hazelcast)
and despite _Spring Boot_ providing [_auto-configuration_ support](https://docs.spring.io/spring-boot/docs/2.0.0.M6/reference/htmlsingle/#boot-features-nosql)
for (again) both [Redis](https://docs.spring.io/spring-boot/docs/2.0.0.M6/reference/htmlsingle/#boot-features-redis)
and [Hazelcast](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/hazelcast).

Of course, _Redis_ is a very popular and powerful data store
that compliments rather than competes directly with _Apache Geode_,
unlike _Hazelcast_.  _Apache Geode_ is a good replacement for
_Hazelcast_ when your application needs more reliable, performant
and consistent data access at cloud-scale.

> NOTE: _Apache Geode_ is the open source core of [_Pivotal GemFire_](https://pivotal.io/pivotal-gemfire).

This step-by-step tutorial guides you through the application
and how it works.


### Tutorial

#### _Prerequisites_

This guide assumes a basic understanding of _Apache Geode_,
IMDG / No-SQL, and _Spring Data_ concepts.

To learn more about _Apache Geode's_, see the
[User Guide](http://geode.apache.org/docs/guide/12/getting_started/book_intro.html).

To learn more about [_Spring Data_](https://docs.spring.io/spring-data/commons/docs/current/reference/html/)
and in particular, [_Spring Data Geode (SDG)_](https://docs.spring.io/spring-data/geode/docs/current/reference/html/),
follow the links.

There are also several examples on using _Spring Data GemFire/Geode_
in the [Guides at spring.io](https://spring.io/guides).


#### _Problem_

Suppose we need to create a customer service application that stores
customer data and allows the user to search for customers by name.


#### _Prototyping the customer service application_

###### _Customer class_

First, we need to define an application domain object to encapsulate
customer information.

````java
@Region("Customers")
class Customer {

  Long id;

  @Indexed(from = "/Customers")
  String name;

}
````

> TIP: The actual `Customer` class definition uses the highly convenient
[Project Lombok](https://projectlombok.org/) framework to simplify
the definition of our `Customer` class.

[`@Region`](https://docs.spring.io/spring-data/geode/docs/current/api/org/springframework/data/gemfire/mapping/annotation/Region.html)
is a _Spring Data Geode_ (SDG) mapping annotation that identifies
the Apache Geode _Region_ where instances of `Customer` will be stored.
If a name (e.g. "Customers") is not explicitly provided in the `@Region`
mapping annotation, then the simple name of the class (i.e. "Customer")
is used to identify the _Region_ where customers are persisted.

> NOTE: SDG [provides several _Region type-specific mapping annotations](https://docs.spring.io/spring-data/geode/docs/current/api/org/springframework/data/gemfire/mapping/annotation/package-summary.html)
giving the developer full control over her data management policy.

SDG is intelligent enough to identify the `id` field as the identifier
for individual customers without explicitly having to annotate
the identifier field or property with _Spring Data's_ [@Id](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/annotation/Id.html)
annotation.

> NOTE: There is no auto-generated identifier capability provided by
_Apache Geode_ nor SDG.  You must set the ID before saving instances
of `Customer` to the "Customers" _Region_.

Finally, you will notice that `Customer` `name` field is annotated with
`@Indexed`.  This enables the creation of an OQL based `Index`
on the customer's name, thereby improving query by name performance.
More on this later.

###### _CustomerRepository interface_

Next, we need to define a _Repository_ implementation to store and query
`Customer` objects in _Apache Geode_.

````java
interface CustomerRepository extends CrudRepository<Customer, Long> {
  ...
}

````

Our `CustomerRepository` extends _Spring Data's_ [_CrudRepository_](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html)
interface, which provides basic CRUD and simply Query operations
backed by SDG's implementation for _Apache Geode_.  Refer to the _Javadoc_ link
above for more details on which data access operations are provided
by `o.s.d.repository.CrudRepository` out-of-the-box.

Of course, you define additional ([OQL-based](http://geode.apache.org/docs/guide/12/developing/querying_basics/chapter_overview.html))
queries simply by defining "query" methods in the `CustomerRepository` interface
and following certain [conventions](https://docs.spring.io/spring-data/geode/docs/current/reference/html/#gemfire-repositories.executing-queries).

###### _Spring Boot Application class_

Now, we just need to define a _Spring Boot_ application class
to get configure everything and run our application.

````java
@SpringBootApplication
@ClientCacheApplication
@EnableEntityDefinedRegions(basePackageClasses = Customer.class, clientRegionShortcut = ClientRegionShortcut.LOCAL)
@EnableGemfireRepositories(basePackageClasses = CustomerRepository.class)
@EnableIndexing
public class SpringBootApacheGeodeClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApacheGeodeClientApplication.class, args);
    }

    ...
}
````

This application is not very interesting at the moment since
the application is not performing any data access operations
using _Apache Geode_.

For this, we will add a simple, non-interactive interface using _Spring Boot's_
`ApplicationRunner` interface, defined as a bean in the _Spring_ context,
which will perform a few data access operations on the "Customers" _Region_
using our `CustomerRepository` that we defined above.

````java
@Bean
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

````

The application is _ready to run_!


#### _But wait! What about configuring _Apache Geode_?_

Well, you may have already noticed, but that was handled by 3
[_Spring Data Geode_ configuration annotations](https://docs.spring.io/spring-data/geode/docs/current/reference/html/#bootstrap-annotation-config).

First is the `@ClientCacheApplication`, which defines both an _Apache Geode_
`ClientCache` instance as well as a "DEFAULT" `Pool`.  The `Pool` is used
to connect to a (cluster of) server(s) in a client/server topology.
We'll see how this works further below.

Next, is `@EnableEntityDefinedRegions`.  This annotation functions much
client [JPA entity-scan](https://docs.spring.io/spring-boot/docs/2.0.0.M6/reference/htmlsingle/#boot-features-entity-classes)
provided in _Spring Boot_ along with _Hibernates_ auto-schema generation,
but without a separate tool!

Additionally, I have set the client _Region_ data management policy
to `LOCAL`, using _Apache Geode's_ [`ClientRegionShortcut`](http://geode.apache.org/releases/latest/javadoc/org/apache/geode/cache/client/ClientRegionShortcut.html).
All this means is that data will be stored locally,
with the client application (at least for now).

> NOTE: _Apache Geode_ stores all data in a cache into [_Regions_](http://geode.apache.org/docs/guide/12/basic_config/data_regions/chapter_overview.html).
You can think of _Apache Geode_ _Regions_ as tables in a relational database.
They hold your applications data (or state).  However, the data that is
stored is objects themselves as opposed to using a relational data model.
There are both advantages and disadvantages to using object stores,
but it is the nature of most key/value stores (or `Map`-like (distributed)
data structures).

I have also used the type-safe `basePackageClasses` attribute to specify
the package containing the entities to scan and for which client _Regions_
will be created.  The provided class definition (i.e. `Customer.class`)
is just used to identify the starting package of the entity-scan.  All
class types contained in the package and all sub-packages will be
searched during the scan.

Then, I have declared the `@EnableGemfireRepositories` annotation to
enable the _Spring Data (Geode) Repository_ infrastructure, thereby
allowing a developer to create _Data Access Objects_ (DAO) based on
only an interface definition.

Finally, I have declared the use of `@EnableIndexing` to automatically
define an _Apache Geode_ OQL `Index` on the customer's name without
having to explicity declare a `Index` bean definition or use a tool
to define an _Apache Geode_ `Index` on our "Customers" _Region_.

Now, let's run the application!


#### _Running The Application_

When you run the `SpringBootApacheGeodeClientApplication` you will
see _Spring Boot_ `1.5.8.RELEASE` startup, _Apache Geode_ log output
during the startup sequence, and  our application print out
some results.

````text
Saving Customer [Customer(id=1, name=Jon Doe)]...
Querying for Customer [SELECT * FROM /Customers WHERE name LIKE '%Doe']...
Customer was [Customer(id=1, name=Jon Doe)]
````

That was easy!


#### _Next Steps_

We were able to store a `Customer` (i.e. "Jon Doe") and retrieve this
`Customer` by name using the application's `CustomerRepository`,
`findByNameLike(:String):Customer` (OQL-based) query method.

But, you might be thinking, "So what!"  Any data store with
a sufficiently robust framework (i.e. _Spring Data_) can do that.

Also, if this application crashes, then I will lose all my data since
this application is not "durable".  And, even if this application
were persistent, I don't want my data kept locally since I am not
really leveraging the full power of _Apache Geode_, as an
_In-Memory Data Grid_ (IMDG), capable of distributing data across
a cluster of nodes in a replicated, highly available (i.e. redundant)
and partitioned manner while preserving strong consistency
and performance (i.e. read/write throughput and latency) guarantees
(no less).

Well, that is simple to do to, :)

You have several options for configuring a (cluster of) server(s).
The most obvious option is to use [_Gfsh_](http://geode.apache.org/docs/guide/12/tools_modules/gfsh/chapter_overview.html),
_Apache Geode's_ Shell tool.

> HINT: _Gfsh_ is equivalent to `sqlplus` for all you _Oracle_ users.

The following is an example _Gfsh_ shell script to bootstrap a small,
yet simple _Apache Geode_ cluster consisting of 2 members, 1 of which
is a server that will store our application's data.

````xml
start locator --name=LocatorOne --log-level=config
start server --name=ServerOne --log-level=config
list members
describe member --name=LocatorOne
describe member --name=ServerOne
create region --name=Customers --type=PARTITION --skip-if-exists
list regions
describe region --name=/Customers
create index --name=CustomerNameHashIdx --expression="name" --region="/Customers" --type=hash
list indexes
````

> NOTE: this _Gfsh_ shell script, along with several other shell scripts,
are provided in ${project.home}/etc and can be executed in _Gfsh_ using...
gfsh> `run --file=/absolute/file/system/path/to/${project.home}/etc/<script-name>
Be sure to replace `<script-name>` with the name and extension of the
Gfsh shell script, e.g. `start-cluster.gfsh`.

In order to leverage _Apache Geode's_ [client/server topology](http://geode.apache.org/docs/guide/12/topologies_and_comm/cs_configuration/chapter_overview.html)
with minimal changes to our application, simply remove
the `clientRegionShortcut` attribute declaration from
the `@EnableEntityDefinedRegions` annotation.  Now, the client application
will expect to send all ("/Customers") _Region_ data access operations
to a server.

> NOTE: the default client _Region_ data management policy is `PROXY`,
which stores no local state and sends all data operations to the server.

> TIP: It is also possible to store data locally, on the client,
(using `CACHING_PROXY`, referred to as a "_near-cache_") as well as
on the server, and for the data to be kept in-sync. The client will
receive any updates for which it has _registered interests_, and can
even be configured with "durability" so that it also receives update
events that it may have missed while off-line, the next time
the client reconnects to the cluster.

Remember our "DEFAULT" `Pool`.  Well, by default, _Apache Geode_ creates
this "DEFAULT" `Pool` to connect to a server running on "_localhost_",
listening on port "_40404_".  Of course, this `Pool` is highly configurable, with
the ability to set min/max connections, Socket timeouts, retry attempts,
as well as connection endpoints, and many other configuration settings.

> TIP: It is recommended that clients connect to 1 or more _Locators_
in the cluster, which allow a client to seamlessly fail over,
load balance, single-hop and route data requests to the appropriate
servers containing the data of interests.

With 1 simple change, your application is now fully client/server
capable.


#### _Final Steps_

While it is **recommended** that you use _Gfsh_ to script your production
configuration deployments, _Gfsh_ is not really a "_developer_" tool
and therefore, is not as convenient as using your IDE, especially
during rapid application development (DEV-TEST) and prototyping purposes.

Imagine if you have an application with 100s of data _Regions_.  It is not
uncommon to have 100 or even 1000s of tables in a relational database.

While you only need to create a _Gfsh_ shell script once, you still need
to handle the client configuration.

This is where _Spring Data Geode_ separates itself from the pack,
and has a powerful advantage, even over other [_Spring Data_ modules](http://projects.spring.io/spring-data/)
and even though some of those SD modules have robust [_auto-configuration_ support in _Spring Boot_](https://docs.spring.io/spring-boot/docs/2.0.0.M6/reference/htmlsingle/#boot-features-nosql).

This all pales in comparison to SDG _auto-configuration_ capabilities,
which were specifically designed for _Apache Geode_, above and beyond
what _Spring Boot_ can or even should provide, which is very data store
specific.

> NOTE: In fact, _Spring Boot_ is not providing any _Apache Geode_
specific functionality; i.e. no extra _magic_, no _auto-configuration_.
It is all _Spring Data Geode_!  There really is no advantage to using
_Spring Boot_ in this use case.  However, it is convenient for
bootstrapping the _Spring_ container along with giving us the ability
to extend our application to be Web-based, or integrate with countless
other technologies (e.g. another data store perhaps, like **_Redis_**,
using _Spring Data Redis_, or maybe a Message bus, our to take advantage
of Cloud-Native design patterns (e.g. microservices) using _Spring Cloud_,
and so on an so forth.  _Spring Boot_ is truly wonderful and magical.

So, what if we want to avoid doing double the work... creating both
_Gfsh_ shell scripts to create _Regions_, _Indexes_, etc, etc
and creating the matching configuration on the client, at least during
development-time, while we are prototyping?

What if we could do all the work from the (client) application?

Well, you can!  Your application domain types tells us everything
your application needs already.  Why should you have to repeat that?
You've already done the work by defining your domain types, Repositories,
and service classes.

Any framework worth its weight in salt should do the heavy-lifting, the
"plumbing" for you!

Well, now _Spring Data Geode_ can!

Simply add the `@EnableClusterConfiguration` annotation to your
_Spring Boot_ application class and you are nearly there!

````java
...
@EnableClusterConfiguration(useHttp = true)
public class SpringBootApacheGeodeClientApplication {
````

Wait! What? "Nearly"!?

Well, guess what?  Anytime you send data over-the-wire, your data,
your application domain object types, need to be "_serializable_".

"_Oh, for <beep> <beeping> <beepity> sakes!  Now you tell me!
Now, I have to go and change all my domain types (all 1 of them ;-)!"

No worries! Most of the time you would think that all your application
domain object classes need to implement `java.io.Serializable`, huh?
Then you have to worry about potentially setting a `serialVersionId`
if the types changes, blah, blah, blah.  Oh, for crying out loud Java!
What a PITA!  Forget that non-sense.

Fortunately, for you, _Apache Geode_ has you covered and provides its
own serialization capabilities, which are far more robust
(and in certain cases) "portable" than _Java Serialization_.

See the [_Data Serialization_ chapter](http://geode.apache.org/docs/guide/12/developing/data_serialization/chapter_overview.html)
in _Apache Geode's User Guide_ for more details.

However, unless you want to read a whole chapter on _Apache Geode's_
data serialization features, just annotate your _Spring Boot_ application
class with `@EnablePdx` annotation, and you are all set.

````java
...
@EnablePdx
@EnableClusterConfiguration(useHttp = true)
public class SpringBootApacheGeodeClientApplication {
````

One advantage of _Apache Geode's_ PDX (_Portable Data EXchange_)
serialization framework is that _Apache Geode_ is able to query data
in PDX serialized form without having to deserialize the data first.
It is also capable fo adding/remove fields without affecting older
clients using older class definitions of those types.
It has many other advantages as well.

So, what just happened here?

The `@EnableClusterConfiguration` annotation enables our _Spring Boot_
client application to push the schema object definitions (both _Regions_
and _Indexes_) to the server.

However, to do so, you do need a full installation of _Apache Geode_
running on the server.

> NOTE: You are not required to have full installation of _Apache Geode_
to run servers.  For instance, you can created _Spring Boot_ based
_Apache Geode_ server applications as well that only include the necessary
JAR files.  But, certain features are not available in this arrangement.

SDG is very careful not to stomp on your existing server cluster
configuration.  That is, if a _Region_ is already defined in the cluster,
then SDG will not attempt to create the _Region_, hopefully for obvious
reasons.

> NOTE: there is not option to drop and recreate a _Region_ at present.
You must remove a _Region_ manually, using _Gfsh_.  You can do this
by using gfsh> `destroy region --name=/Customers`.

Not only does `@EnableClusterConfiguration` push the schema object
definitions to the server(s) in the cluster, but also does so in such
a way that the schema changes are recorded and remembered.

Therefore, if you add a new server, it will have the same configuration,
thereby allowing to easily and quickly scale up your system architecture.

Just start another server in _Gfsh_ using...

````xml
gfsh> start server --name=ServerTwo --log-level=config --disable-default-server
````

You can keep adding servers to the cluster until your heart is content!
They will all have the same configuration.

If your entire cluster goes down, rest assured that when your bring
the servers back up, they will retain their configuration.

> TIP: you may also change the server _Region_ data management policy
used on the servers(s) in the cluster when creating the _Regions_.
By default, it the server Region data management policy is set to
`PARTITION`.  To change the data management policy (such as to make
the server _Regions_ persistent) then set the `serverRegionShortcut`
attribute in the `@EnableClusterConfiguration` annotation, like so...
`@EnableClusterConfiguration(useHttp = true, serverRegionShortcut = RegionShortcut.PARTITION_PERSISTENT)`.
Not only will the server retain their configuration, but now they will retain your data too!

To test our this scenario, I have provided another _Gfsh_ shell script
to create an "empty" cluster (i.e. cluster with a server having
no _Regions_ or _Indexes_).  Simply run the _etc/start-empty-cluster.gfsh_
file from _Gfsh_ using...

````xml
gfsh> run --file=/absolute/file/system/path/to/${project.home}/etc/start-empty-cluster.gfsh
````

Then, run the _Spring Boot_ application, which should be
currently defined as...

````java
@SpringBootApplication
@ClientCacheApplication
@EnableEntityDefinedRegions(basePackageClasses = Customer.class, clientRegionShortcut = ClientRegionShortcut.LOCAL)
@EnableGemfireRepositories(basePackageClasses = CustomerRepository.class)
@EnableIndexing
@EnablePdx
@EnableClusterConfiguration(useHttp = true)
public class SpringBootApacheGeodeClientApplication {
  ...
}
````

With 6 simple configuration-based annotations...

* `@ClientCacheApplication`
* `@EnableEntityDefinedRegions(..)`
* `@EnableGemfireRepositories(..)`
* `@EnableIndexing`
* `@EnablePdx`
* `@EnableClusterConfiguration`

... SDG has unleashed a profound amount of power here, reducing much of
the error-prone, boilerplate, highly redundant task of performing
the required plumbing needed by your applications to even _get started_.

We went from 0 to a fully cluster-capable application in very few
lines of code.

The net effect is a **greatly simplified _getting started_ experience**,
staying true to _Spring's_ promise and commitment of developer productivity.

And, this is just the tip of the iceberg.

#### _Where To Go From Here_

Be on the lookup for more example tutorials like this, showcasing many
different, even more complex use cases.

Minimally, be sure to read ["_Chapter 6 - Bootstrapping Apache Geode using Spring Annotations_"](https://docs.spring.io/spring-data/geode/docs/current/reference/html/#bootstrap-annotation-config)
in the [_Spring Data Geode Reference Documentation_](https://docs.spring.io/spring-data/geode/docs/current/reference/html/).

You can also use this [repository](https://github.com/jxblum/contacts-application) (WIP),
containing several other examples, as a reference on how to use other
_Aapche Geode_ features (e.g. _Continuous Query_) from _Spring_.

### Conclusion

Thank you for reading this tutorial.

If you have questions or issues, please ask your questions on [StackOverflow](https://stackoverflow.com/questions/tagged/spring-data-gemfire)
and [file an Issue](https://github.com/jxblum/simple-spring-geode-application/issues) in _GitHub_.

You are also welcomed to [contribute PRs](https://github.com/jxblum/simple-spring-geode-application/pulls)
to this example as well, if you see areas for improvement.
