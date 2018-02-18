[![Build status](https://travis-ci.org/konrad-kaminski/spring-kotlin-coroutine.svg?branch=master)](https://travis-ci.org/konrad-kaminski/spring-kotlin-coroutine)
[![Download](https://api.bintray.com/packages/konrad-kaminski/maven/spring-kotlin-coroutine/images/download.svg)](https://bintray.com/konrad-kaminski/maven/spring-kotlin-coroutine/_latestVersion)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

`spring-kotlin-coroutine` is a repository that contains several libraries and a demo app which allows using Kotlin coroutines in 
Spring applications as first-class citizens.

## Project modules

This project contains several modules:
1. `spring-kotlin-coroutine` - a library which allow using Kotlin coroutines in Spring applications. It contains support
   for [`@Coroutine`](#coroutine) annotation, [application events](#application-events), [caching](#cacheable-support),
   [scheduled tasks](#scheduled-support), [`CoroutineRestOperations`](#coroutinerestoperations), [`DeferredRestOperations`](#deferredrestoperations),
   [`ListenableFuture` extensions](#listenablefuture-extensions).
2. `spring-webmvc-kotlin-coroutine` - a library which allow using Kotlin coroutines in Spring MVC applications. It contains
   support for [web methods](#web-methods).
3. `spring-webflux-kotlin-coroutine` - a library which allow using Kotlin coroutines in Spring Web Flux applications. It contains
   support for [Web Flux web methods](#web-flux-web-methods), [`CoroutineWebClient`](#coroutinewebclient) and
   [functional style routes definition](#functional-style-routes-definition).
4. `spring-data-mongodb-kotlin-coroutine` - a library which allow using Kotlin coroutines in Spring Data Mongo applications. It contains
   support for [`CoroutineMongoRepository`](#coroutinemongorepository) and [`CoroutineMongoTemplate`](#coroutinemongotemplate).
5. `spring-boot-autoconfigure-kotlin-coroutine` - a library which contains autoconfiguration support for `spring-data-mongodb-kotlin-coroutine`
   via [`@EnableCoroutineMongoRepositories` annotation](#enablecoroutinemongorepositories).  
6. `spring-kotlin-coroutine-demo` - contains a sample application which demonstrates the use of `spring-kotlin-coroutine` and
    `spring-webmvc-kotlin-coroutine`.                             

## Supported features

Most of the supported features require adding an [`@EnableCoroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/EnableCoroutine.kt) annotation to your Spring 
[`@Configuration`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html) class.

### @EnableCoroutine

This annotation enables using the features described below. If it is possible to use a feature without this annotation
it is explicitly stated so in the feature description. The annotation can be used as follows:

```kotlin
@Configuration
@EnableCoroutine(
    proxyTargetClass = false, mode = AdviceMode.PROXY, 
    order = Ordered.LOWEST_PRECEDENCE, schedulerDispatcher = "")
open class MyAppConfiguration {
 ...
}
```

The `proxyTargetClass`, `mode` and `order` attributes of [`@EnableCoroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/EnableCoroutine.kt) follow the same semantics as [`@EnableCaching`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/annotation/EnableCaching.html).
`schedulerDispatcher` is a [`CoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-coroutine-dispatcher/)
used to run [`@Scheduled`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html) corroutines.

> Note that currently only [`AdviceMode.PROXY`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/AdviceMode.html#PROXY) mode is supported.

### Web methods

Because coroutines can be suspended and they have an additional implicit callback parameter they cannot be used
as web methods by default. With special parameter and result handling introduced in `spring-webmvc-kotlin-coroutine`
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
Spring beans and its methods can be annotated with [`@Coroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/annotation/Coroutine.kt). Using this annotation you can specify
the _coroutine context_ via the `context` attribute and the [_coroutine name_](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-coroutine-name/index.html) via the `name` attribute. 
The `context` specifies a name of a bean from which a [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) can be created. Currently the following
contexts/bean types are supported:

1. [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) type beans - used directly
2. `COMMON_POOL` - a constant specifying the [`CommonPool`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-common-pool/index.html) context
2. `DEFAULT_DISPATCHER` - a constant specifying the [`DefaultDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-default-dispatcher.html) context
3. `UNCONFINED` - a constant specifying the [`Unconfined`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-unconfined/index.html) context
4. [`Executor`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html) type beans - converted to [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) with [`asCoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/java.util.concurrent.-executor/as-coroutine-dispatcher.html)
5. Rx1 [`Scheduler`](http://reactivex.io/RxJava/javadoc/io/reactivex/Scheduler.html) type beans - converted to [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) with [`asCoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-rx1/kotlinx.coroutines.experimental.rx1/rx.-scheduler/as-coroutine-dispatcher.html)
6. Rx2 [`Scheduler`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Scheduler.html) type beans - converted to [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) with [`asCoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-rx2/kotlinx.coroutines.experimental.rx2/io.reactivex.-scheduler/as-coroutine-dispatcher.html)
7. Reactor [`Scheduler`](https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Scheduler.html) type beans - converted to [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) with [`asCoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-reactor/kotlinx.coroutines.experimental.reactor/reactor.core.scheduler.-scheduler/as-coroutine-dispatcher.html)
8. [`TaskScheduler`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/TaskScheduler.html) type beans - converted to [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) with `asCoroutineDispatcher` method of [`TaskSchedulerCoroutineContextResolver`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/context/resolver/TaskSchedulerCoroutineContextResolver.kt)
   
You can also support your own types of beans or context names by providing Spring beans of type [`CoroutineContextResolver`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/context/CoroutineContextResolver.kt):
   
```kotlin
interface CoroutineContextResolver {
    fun resolveContext(beanName: String, bean: Any?): CoroutineContext?
}
```   

Using [`@Coroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/annotation/Coroutine.kt) it is quite easy to achieve the same effect as with [`@Async`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/Async.html), although the code will look much simpler:

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

Your bean can inject this interface or it can implement a [`CoroutineApplicationEventPublisherAware`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/event/CoroutineApplicationEventPublisherAware.kt) interface and have
it delivered via a `setCoroutineApplicationEventPublisher` method:

```kotlin
interface CoroutineApplicationEventPublisherAware : Aware {
    fun setCoroutineApplicationEventPublisher(publisher: CoroutineApplicationEventPublisher)
}
```

The events sent by either [`CoroutineApplicationEventPublisher`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/event/CoroutineApplicationEventPublisher.kt) or [`ApplicationEventPublisher`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/ApplicationEventPublisher.html) can be received 
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

### @Scheduled support

Coroutines annotated with [`@Scheduled`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html) will
not work with regular Spring. However, with `spring-kotlin-coroutine` you can use them the same way you would do it with regular methods with the following caveats:
1. They will be executed using [`CoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-coroutine-dispatcher/)
   obtained from:
   * `schedulerDispatcher` attribute of [`@EnableCoroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/EnableCoroutine.kt) annotation (if a custom value is specified) - it works the same way as the
     `context` attribute of [`@Coroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/annotation/Coroutine.kt) annotation (see [`@Coroutine` section](#coroutine)), or
   *  [`TaskScheduler`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/TaskScheduler.html) [used by `ScheduledTaskRegistrar`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/config/ScheduledTaskRegistrar.html#getScheduler--) from the [`ScheduledAnnotationBeanPostProcessor#registrar`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/ScheduledAnnotationBeanPostProcessor.html) 
      converted into [`TaskSchedulerDispatcher`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/context/resolver/TaskSchedulerCoroutineContextResolver.kt) (which is a [`CoroutineDispatcher`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-coroutine-dispatcher/) 
      converted with [`TaskSchedulerCoroutineContextResolver`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/context/resolver/TaskSchedulerCoroutineContextResolver.kt)) - this mimics the default behaviour of Spring for regular [`@Scheduled`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html) 
      annotated method.
2. The exception thrown from the coroutine will be handled using:
   * [Default](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/support/TaskUtils.html#getDefaultErrorHandler-boolean-) [`ErrorHandler`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/util/ErrorHandler.html) for repeating tasks if [`TaskSchedulerDispatcher`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/context/resolver/TaskSchedulerCoroutineContextResolver.kt) is used,
   * [`handleCoroutineException`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/handle-coroutine-exception.html) otherwise.

### CoroutineRestOperations

Spring provides blocking [`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html) to be used as a `REST` client. `spring-kotlin-coroutine` provides 
[`CoroutineRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/CoroutineRestOperations.kt) interface which has the same methods as [`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html), but as coroutines:

```kotlin
interface CoroutineRestOperations {
    suspend fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, vararg uriVariables: Any?): T

    suspend fun <T : Any?> postForObject(url: String, request: Any?, responseType: Class<T>?, uriVariables: Map<String, *>): T

    suspend fun <T : Any?> postForObject(url: URI, request: Any?, responseType: Class<T>?): T

    ...
}
```

In order to create [`CoroutineRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/CoroutineRestOperations.kt) use the following:
 
```kotlin
val restOps = CoroutineRestOperations(restOperations = RestTemplate(), context = null)
val defaultRestOps = CoroutineRestOperations() 
``` 

If you do not specify any arguments when creating [`CoroutineRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/CoroutineRestOperations.kt) it will delegate all calls to [`RestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
instance and use a special _coroutine context_ which will invoke the blocking method of [`RestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html) on a separate thread
 (just like [`AsyncRestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/AsyncRestTemplate.html)). You can specify your own [`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html) and [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) to change that behaviour.

> Note that [`CoroutineRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/CoroutineRestOperations.kt) does not need [`@EnableCoroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/EnableCoroutine.kt) in order to work. Underneath [`CoroutineRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/CoroutineRestOperations.kt)
uses [`createCoroutineProxy`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/proxy/CoroutineProxy.kt).  

### DeferredRestOperations

The [`kotlinx.coroutines`](https://github.com/Kotlin/kotlinx.coroutines) library provides [`Deferred<T>`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-deferred/index.html) type 
for non-blocking cancellable future. Based on that `spring-kotlin-coroutine` provides [`DeferredRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/DeferredRestOperations.kt) interface which has the same methods as 
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

In order to create [`DeferredRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/DeferredRestOperations.kt) use the following:
 
```kotlin
val restOps = DeferredRestOperations(restOperations = RestTemplate(), start = CoroutineStart.DEFAULT, context = COMMON_POOL)
val defaultRestOps = DeferredRestOperations() 
``` 

If you do not specify any arguments when creating [`DeferredRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/DeferredRestOperations.kt) it will delegate all calls to [`RestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
instance and use a special _coroutine context_ which will immediately invoke the blocking method of [`RestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html) on a separate thread
 (just like [`AsyncRestTemplate`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/AsyncRestTemplate.html)). You can specify your own [`RestOperations`](http://docs.spring.io/spring/docs/current/javadoc-api/index.html?org/springframework/web/client/RestOperations.html) and [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/) to change that behaviour.
By changing the `start` parameter value you can specify when the REST operation should be invoked (see [`CoroutineStart`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-coroutine-start/index.html) for details).

> Note that [`DeferredRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/DeferredRestOperations.kt) does not need [`@EnableCoroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/EnableCoroutine.kt) in order to work. Underneath [`DeferredRestOperations`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/web/client/DeferredRestOperations.kt)
uses [`createCoroutineProxy`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/proxy/CoroutineProxy.kt).  

### Web Flux web methods

By using `spring-webflux-kotlin-coroutine` module instead of `spring-webmvc-kotlin-coroutine` web methods which are
suspending functions will use Spring Web Flux. This enables them to use non-blocking I/O API. 

### CoroutineWebClient

`CoroutineWebClient` is a counterpart of the Spring Web Flux `WebClient` component. The differences between these components
can be found mainly in the functions which operate on reactive types - in `CoroutineWebClient` they are suspending functions
operating on regular types. Also the naming of the methods can be slightly different (e.g. in `WebClient` you can find `bodyToMono`
and in `CoroutineWebClient` it is simply `body`).

### Functional style routes definition

TBD

### CoroutineMongoRepository

`spring-data-mongodb-kotlin-coroutine` contains support for `CoroutineMongoRepository`-based repositories. These repositories
work as regular Spring Data Mongo or Spring Data Mongo Reactive repositories, but have support for suspending functions
and `ReceiveChannel` type.

### CoroutineMongoTemplate

`CoroutineMongoTemplate` is a counterpart of regular `MongoTemplate`, but contains suspending functions instead of regular ones.

### EnableCoroutineMongoRepositories

The `@EnableCoroutineMongoRepositories` annotation works just like `@EnableMongoRepositories` annotation, but enables
the usage of `CoroutineMongoRepository` repositories.

### ListenableFuture extensions

The [`kotlinx.coroutines`](https://github.com/Kotlin/kotlinx.coroutines) library provides interoperability functions with many existing asynchronous libraries (
[`RxJava v1`](https://github.com/ReactiveX/RxJava/tree/1.x), [`RxJava v2`](https://github.com/ReactiveX/RxJava),
[`CompletableFuture`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html), etc.). However, there is no support for Spring specific [`ListenableFuture`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html) interface. Therefore
`spring-kotlin-coroutine` provides the following features:

1. `fun <T> listenableFuture(context: CoroutineContext = DefaultDispatcher, block: suspend () -> T): ListenableFuture<T>` - it 
   allows you to create a `ListenableFuture` from a coroutine with specific _coroutine context_. 
2. `fun <T> Deferred<T>.asListenableFuture(): ListenableFuture<T>` - it allows you to create a `ListenableFuture` from
    Kotlin coroutine specific `Deferred` type.
3. `suspend fun <T> ListenableFuture<T>.await(): T` - it allows you to create a coroutine from a `ListenableFuture`.    

> Note that these extensions do not need [`@EnableCoroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/EnableCoroutine.kt) in order to work.

### Utility functions

> Note that utility functions do not need [`@EnableCoroutine`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/EnableCoroutine.kt) in order to work.

#### createCoroutineProxy

[`createCoroutineProxy`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/proxy/CoroutineProxy.kt) can be used to create a smart proxy - an instance of an interface which will delegate all
function invocations to a regular object with matching method signatures. The runtime characteristics of this
proxy call depends on the types of the interface methods, the types of the proxied object methods and
the proxy config. The [`createCoroutineProxy`](spring-kotlin-coroutine/src/main/kotlin/org/springframework/kotlin/experimental/coroutine/proxy/CoroutineProxy.kt) is declared as follows:

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

## Using in your projects

> Note that this library is experimental and is subject to change.

The library is published to [konrad-kaminski/maven](https://bintray.com/konrad-kaminski/maven/spring-kotlin-coroutine) Bintray repository.

### Gradle

Add Bintray repository:

```groovy
repositories {
  maven { url 'https://dl.bintray.com/konrad-kaminski/maven' }
}
```

Add dependencies:

```groovy
compile 'org.springframework.kotlin:spring-kotlin-coroutine:3'
```

> Note that some of the dependencies of `spring-kotlin-coroutine` are declared as optional. You should declare them as 
runtime dependencies of your application if you want to use the features that require them. The table below contains the 
details:
>
> | Feature                                                                                                                                | Dependency                                                |
> |----------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
> | Web methods                                                                                                                            | `org.springframework:spring-webmvc:5.0.3.RELEASE`         |
> | Rx1 [`Scheduler`](http://reactivex.io/RxJava/javadoc/rx/Scheduler.html) in `@Coroutine`                                                | `org.jetbrains.kotlinx:kotlinx-coroutines-rx1:0.22.1`     |
> | Rx2 [`Scheduler`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Scheduler.html) in `@Coroutine`                                  | `org.jetbrains.kotlinx:kotlinx-coroutines-rx2:0.22.1`     |
> | Reactor [`Scheduler`](https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Scheduler.html) in `@Coroutine`           | `org.jetbrains.kotlinx:kotlinx-coroutines-reactor:0.22.1` |

And make sure that you use the right Kotlin version:

```groovy
buildscript {
    ext.kotlin_version = '1.2.21'
}
```

## FAQ

### Why all the methods/classes have "Coroutine" as part of its name and not "Suspending"? 

It's a deliberate choice. In most cases _Coroutine_ just sounded better to me and even though sometimes _Suspending_
might've been a better choice for consistency _Coroutine_ was used.

## License
`spring-kotlin-coroutine` is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
