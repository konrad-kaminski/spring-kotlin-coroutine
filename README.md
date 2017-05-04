[![Build status](https://travis-ci.org/konrad-kaminski/spring-kotlin-coroutine.svg?branch=master)](https://travis-ci.org/konrad-kaminski/spring-kotlin-coroutine)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

`spring-kotlin-coroutine` is a repository that contains a library and a demo app which allows using Kotlin coroutines in 
Spring applications as first-class citizens.

## Project modules

This project contains two modules:
1. `spring-kotlin-coroutine` - contains code for a library with Spring components and Kotlin extension functions which allow using
                               Kotlin coroutines in Spring applications.
2. `spring-kotlin-coroutine-demo` - contains a sample application which demonstrates the use `spring-kotlin-coroutine`                             

## Supported features

Most of the supported features require adding an `@EnableCoroutine` annotation to your Spring 
[`@Configuration`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html) class.

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

The attributes of `@EnableCoroutine` follow the same semantics as e.g. [`@EnableCaching`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/annotation/EnableCaching.html). 

**NOTE** Currently only [`AdviceMode.PROXY`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/AdviceMode.html#PROXY) mode is supported.

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
the _coroutine context_ via the `context` attribute and the [_coroutine name_](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-coroutine-name/index.html) via the `name` attribute. 
The `context` specifies a name of a bean from which a [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) can be created. Currently the following
contexts/bean types are supported:

1. [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) type beans - used directly
2. `COMMON_POOL` - a constant specifying the [`CommonPool`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-common-pool/index.html) context
3. `UNCONFINED` - a constant specifying the [`Unconfined`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-unconfined/index.html) context
4. [`Executor`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html) type beans - converted to [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) with [`asCoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/java.util.concurrent.-executor/as-coroutine-dispatcher.html)
5. Rx2 [`Scheduler`](http://reactivex.io/RxJava/javadoc/io/reactivex/Scheduler.html) type beans - converted to [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) with [`asCoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-rx2/kotlinx.coroutines.experimental.rx2/io.reactivex.-scheduler/as-coroutine-dispatcher.html)
   
You can also support your own types of beans or context names by providing Spring beans of type `CoroutineContextResolver`:
   
```kotlin
interface CoroutineContextResolver {
    fun resolveContext(beanName: String, bean: Any?): CoroutineContext?
}
```   

Using `@Coroutine` it is quite easy to achieve the same effect as with [`@Async`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/Async.html), although the code will look much simpler:

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

Spring allows you to decouple senders and receivers of application events with the usage of [`ApplicationEventPublisher`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/ApplicationEventPublisher.html) and
[`@EventListener`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/event/EventListener.html) methods. They cannot, however, be coroutines. `spring-kotlin-coroutine` allows you to send
events in a way that allows the suspension of event processing. You can also have an [`@EventListener`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/event/EventListener.html) method which is a
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

The events sent by either `CoroutineApplicationEventPublisher` or [`ApplicationEventPublisher`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/ApplicationEventPublisher.html) can be received 
by any method annotated with [`@EventListener`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/event/EventListener.html) (a coroutine or a regular one). The result of coroutine listeners will
be handled in the same way as for regular listeners:
* if it is a [`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/) nothing will happen
* if it returns a single value it will be treated as a newly published event
* if it returns an array or a collection of values it will be treated as a collection of newly published events

### @Cacheable support

Due to an additional callback parameter and a special return value semantics coroutine return values cannot be cached using
default Spring caching feature. However with `spring-kotlin-coroutine` it is possible to use a [`@Cacheable`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/annotation/Cacheable.html) annotation 
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

Spring provides blocking [`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html) to be used as a `REST` client. `spring-kotlin-coroutine` provides 
`CoroutineRestOperations` interface which has the same methods as [`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html), but as coroutines:

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

If you do not specify any arguments when creating `CoroutineRestOperations` it will delegate all calls to [`RestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
instance and use a special _coroutine context_ which will invoke the blocking method of [`RestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html) on a separate thread
 (just like [`AsyncRestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/AsyncRestTemplate.html)). You can specify your own [`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html) and [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) to change that behaviour.

**NOTE** `CoroutineRestOperations` does not need `@EnableCoroutine` in order to work. Underneath `CoroutineRestOperations`
uses `createCoroutineProxy`.  

### DeferredRestOperations

The [`kotlinx.coroutines`](https://github.com/Kotlin/kotlinx.coroutines) library provides [`Deferred<T>`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-deferred/index.html) type 
for non-blocking cancellable future. Based on that `spring-kotlin-coroutine` provides `DeferredRestOperations` interface which has the same methods as 
[`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html), but with [`Deferred<T>`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-deferred/index.html) 
return type:

```kotlin
interface DeferredRestOperations {
    fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): Deferred<T>

    fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): Deferred<T>

    fun <T : Any?> postForObject(url: URI, request: Any?, responseType: Class<T>?): Deferred<T>

    ...
}
```

In order to create `DeferredRestOperations` use the following:
 
```kotlin
val restOps = DeferredRestOperations(restOperations = RestTemplate(), start = CoroutineStart.DEFAULT, context = COMMON_POOL)
val defaultRestOps = DeferredRestOperations() 
``` 

If you do not specify any arguments when creating `DeferredRestOperations` it will delegate all calls to [`RestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
instance and use a special _coroutine context_ which will immediately invoke the blocking method of [`RestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html) on a separate thread
 (just like [`AsyncRestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/AsyncRestTemplate.html)). You can specify your own [`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html) and [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) to change that behaviour.
By changing the `start` parameter value you can specify when the REST operation should be invoked (see [`CoroutineStart`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-coroutine-start/index.html) for details).

**NOTE** `DeferredRestOperations` does not need `@EnableCoroutine` in order to work. Underneath `DeferredRestOperations`
uses `createCoroutineProxy`.  

### ListenableFuture extensions

The [`kotlinx.coroutines`](https://github.com/Kotlin/kotlinx.coroutines) library provides interoperability functions with many existing asynchronous libraries (
[`RxJava v1`](https://github.com/ReactiveX/RxJava/tree/1.x), [`RxJava v2`](https://github.com/ReactiveX/RxJava),
[`CompletableFuture`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html), etc.). However, there is no support for Spring specific [`ListenableFuture`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html) interface. Therefore
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

`createCoroutineProxy` can be used to create a smart proxy - an instance of an interface which will delegate all
function invocations to a regular object with matching method signatures. The runtime characteristics of this
proxy call depends on the types of the interface methods, the types of the proxied object methods and
the proxy config. The `createCoroutineProxy` is declared as follows:

```kotlin
fun <T: Any> createCoroutineProxy(coroutineInterface: Class<T>, obj: Any, proxyConfig: CoroutineProxyConfig): T
```

Currently supported proxy types are as follows:

| Coroutine interface method     | Object method    | Proxy config                   |
|--------------------------------|------------------|--------------------------------|
| `suspend fun m(a: A): T`       | `fun m(a: A): T` | `DefaultCoroutineProxyConfig`  |
| `fun <T> m(a: A): Deferred<T>` | `fun m(a: A): T` | `DeferredCoroutineProxyConfig` |

#### Method.isSuspend

`Method.isSuspend` allows you to check if a method is a coroutine. It is defined as follows:

```kotlin
val Method.isSuspend: Boolean
```

## FAQ

### Why all the methods/classes have "Coroutine" as part of its name and not "Suspending"? 

It's a deliberate choice. In most cases _Coroutine_ just sounded better to me and even though sometimes _Suspending_
might've been a better choice for consistency _Coroutine_ was used.


## License
`spring-kotlin-coroutine` is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
