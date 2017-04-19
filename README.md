`spring-kotlin-coroutine` is a repository that contains a library and a demo app which allows using Kotlin coroutines in 
Spring applications as first-class citizens.

## Project modules

This project contains two modules:
1. `spring-kotlin-coroutine` - contains code for a library with Spring components and Kotlin extension functions which allow using
                               Kotlin coroutines in Spring applications.
2. `spring-kotlin-coroutine-demo` - contains a sample application which demonstrates the use `spring-kotlin-coroutine`                             

## Supported features

Most of the supported features require adding an `@EnableCoroutine` annotation to your Spring `@Configuration` class.

### @EnableCoroutine

This annotation enables using the features described below. If it is possible to use a feature without this annotation
it is explicitly stated so in the feature description. The annotation can be used as follows:

```kotlin
@Configuration
@EnableCoroutine(proxyTargetClass = false, mode = AdviceMode.PROXY, order = Ordered.LOWEST_PRECEDENCE)
open class MyAppConfiguration {
 ...
}
```

The attributes of `@EnableCoroutine` follow the same semantics as e.g. `@EnableCaching`. 

**NOTE** Currently only `AdviceMode.PROXY` mode is supported.

### Web methods

Because coroutines can be suspended and they have an additional implicit callback parameter they cannot be used
as web methods by default. With special parameter and result handling introduced in `spring-kotlin-coroutine`
you can safely use coroutines as web methods. You can e.g. have the following component:

```kotlin
@RestController
open class MyController {
    @GetMapping("/customers")
    suspend open fun getCustomers(): List<Customer> {
       ...
    }
}
```

### @Coroutine
Spring beans and its methods can be annotated with `@Coroutine`. Using this annotation you can specify
the _coroutine context_ via the `context` attribute and the _coroutine name_ via the `name` attribute. 
The `context` specifies a name of a bean from which a `CoroutineContext` can be created. Currently the following
contexts/bean types are supported:

1. `CoroutineContext` type beans - used directly
2. `COMMON_POOL` - a constant specifying the `CommonPool` context
3. `UNCONFINED` - a constant specifying the `Unconfined` context
4. `Executor` type beans - converted to `CoroutineContext` with `asCoroutineDispatcher`
5. Rx2 `Scheduler` type beans - converted to `CoroutineContext` with `asCoroutineDispatcher`
   
You can also support your own types of beans or context names by providing Spring beans of type `CoroutineContextResolver`:
   
```kotlin
interface CoroutineContextResolver {
    fun resolveContext(beanName: String, bean: Any?): CoroutineContext?
}
```   

Using `@Coroutine` it is quite easy to achieve the same effect as with `@Async`, although the code will look much simpler:

```kotlin
@RestController
open class MyController(
    private val repository: MyRepository
) {
    @GetMapping("/customers")
    suspend open fun getCustomers(): List<Customer> {
       return repository.getAllCustomers()
    }
}

@Component
@Coroutine(COMMON_POOL)
open class MyRepository {
    suspend open fun getAllCustomers(): List<Customer> {
      // a blocking code
      // which will be run with COMMON_POOL context
      // or simply ForkJoinPool
      ...
    }
}
```

### Application events

Spring allows you to decouple senders and receivers of application events with the usage of `ApplicationEventPublisher` and
`@EventListener` methods. They cannot, however, be coroutines. `spring-kotlin-coroutine` allows you to send
events in a way that allows the suspension of event processing. You can also have an `@EventListener` method which is a
coroutine.

For sending a component with the following interface can be used:

```kotlin
interface CoroutineApplicationEventPublisher {
    suspend fun publishEvent(event: ApplicationEvent)

    suspend fun publishEvent(event: Any)
}
```

Your bean can inject this interface or it can implement a `CoroutineApplicationEventPublisherAware` interface and have
it delivered via a `setCoroutineApplicationEventPublisher` method:

```kotlin
interface CoroutineApplicationEventPublisherAware : Aware {
    fun setCoroutineApplicationEventPublisher(publisher: CoroutineApplicationEventPublisher)
}
```

The events sent by either `CoroutineApplicationEventPublisher` or `ApplicationEventPublisher` can be received 
by any method annotated with `@EventListener` (a coroutine or a regular one). The result of coroutine listeners will
be handled in the same way as for regular listeners:
* if it is a `Unit` nothing will happen
* if it returns a single value it will be treated as a newly published event
* if it returns an array or a collection of values it will be treated as a collection of newly published events

### @Cacheable support

Due to an additional callback parameter and a special return value semantics coroutine return values cannot be cached using
default Spring caching feature. However with `spring-kotlin-coroutine` it is possible to use a `@Cacheable` annotation 
on a coroutine, e.g.:

```kotlin
@Configuration
@EnableCaching
@EnableCoroutine
open class MyConfiguration {
}

@Component
class MyComponent {
    @Cacheable
    open suspend fun getCustomer(id: String): Customer {
        ...
    }
}
```

### CoroutineRestOperations

Spring provides blocking `RestOperations` to be used as a `REST` client. `spring-kotlin-coroutine` provides 
`CoroutineRestOperations` which have the same methods as `RestOperations`, but as coroutines:

```kotlin
interface CoroutineRestOperations {
    suspend fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): T

    suspend fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): T

    suspend fun <T : Any?> postForObject(url: URI, request: Any?, responseType: Class<T>?): T

    ...
}
```

In order to create `CoroutineRestOperations` use the following:
 
```kotlin
val restOps = CoroutineRestOperations(restOperations = RestTemplate(), context = null)
val defaultRestOps = CoroutineRestOperations() 
``` 

If you do not specify any arguments when creating `CoroutineRestOperations` it will delegate all calls to `RestTemplate`
instance and use a special _coroutine context_ which will invoke the blocking method of `RestTemplate` on a separate thread
 (just like `AsyncRestTemplate`). You can specify your own `RestOperations` and `CoroutineContext` to change that behaviour.

**NOTE** `CoroutineRestOperations` do not need `@EnableCoroutine` in order to work. Underneath `CoroutineRestOperations`
uses `createCoroutineProxy`.  

### ListenableFuture extensions

The `kotlinx.coroutines` library provides interoperability functions with many existing asynchronous libraries (Rx1, Rx2,
`CompletableFuture`, etc.). However, there is no support for Spring specific `ListenableFuture` interface. Therefore
`spring-kotlin-coroutine` provides the following features:

1. `fun <T> listenableFuture(context: CoroutineContext = CommonPool, block: suspend () -> T): ListenableFuture<T>` - it 
   allows you to create a `ListenableFuture` from a coroutine with specific _coroutine context_. 
2. `fun <T> Deferred<T>.asListenableFuture(): ListenableFuture<T>` - it allows you to create a `ListenableFuture` from
    Kotlin coroutine specific `Deferred` type.
3. `suspend fun <T> ListenableFuture<T>.await(): T` - it allows you to create a coroutine from a `ListenableFuture`.    

**NOTE** These extensions do not need `@EnableCoroutine` in order to work.

### Utility functions

**NOTE** The utility functions do not need `@EnableCoroutine` in order to work.

#### createCoroutineProxy

`createCoroutineProxy` can be used to create an instance of an interface with coroutines which will delegate all
function invocations to a regular object with matching method signatures. The `createCoroutineProxy` is defined
as follows:

```kotlin
fun <T: Any> createCoroutineProxy(coroutineInterface: Class<T>, obj: Any, context: CoroutineContext? = null): T
```

#### Method.isSuspend

`Method.isSuspend` allows you to check if a method is a coroutine. It is defined as follows:

```kotlin
val Method.isSuspend: Boolean
```

## FAQ

### Why all the methods/classes have "Coroutine" as part of its name and not "Suspending"? 

It's a deliberate choice. In most cases _Coroutine_ just sounded better to me and even though sometimes _Suspending_
might've been a better choice for consistency _Coroutine_ was used.