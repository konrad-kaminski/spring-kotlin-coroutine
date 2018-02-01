## 0.3.2
* Support for Kotlin 1.2.21, kotlinx-coroutines 0.22.1, Spring 5.0.3.RELEASE, Spring Data 2.0.3.RELEASE, Spring Boot 2.0.0.RC1, Gradle 4.5.
* Support for header methods in `CoroutineWebClient` (#13).

## 0.3.1
* CoroutineMongoTemplate now returns List<T> in most operations instead of ReceiveChannel<T>.
* A workaround for missing optional dependencies.
* uri methods in CoroutineWebClient.

## 0.3.0
* Introduced support for Spring WebFlux based coroutines, functional style routing, CoroutineMongoRepository,
  CoroutineMongoTemplate, CoroutineWebClient.

## 0.2.3
* New version of kotlinx-coroutines.
* Dropped support for deprecated Reactor [`TimedScheduler`]()

## 0.2.2
* New versions of: Spring, Spring Boot, Kotlin, kotlinx-coroutines, Gradle.

## 0.2.1
* Rx1 [`Scheduler`](http://reactivex.io/RxJava/javadoc/rx/Scheduler.html) can now be converted into [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/).

## 0.2.0

* Added [`@Scheduled`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html) support.
* [`TaskScheduler`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/TaskScheduler.html) can now be converted into [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines.experimental/-coroutine-context/).

## 0.1.0

* Initial version.
