/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.mongodb.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.config.EnableCoroutineMongoRepositories
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll
import test.TestEntity
import test.TestRepository
import test.TestRepository2
import test.TestRepository3
import test.TestRepository4

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import static org.springframework.kotlin.experimental.coroutine.TestUtilsKt.asList
import static org.springframework.kotlin.experimental.coroutine.TestUtilsKt.runBlocking

@SpringBootTest(classes = IntSpecConfiguration, webEnvironment = NONE)
@EnableAutoConfiguration
@EnableCoroutineMongoRepositories(basePackages = "test")
class CoroutineMongoRepositoryIntSpec extends Specification {

    static private def ENTITY_ID = "id-1"

    @Autowired
    private TestRepository testRepository

    @Autowired
    private TestRepository2 testRepository2

    @Autowired
    private TestRepository3 testRepository3

    @Autowired
    private TestRepository4 testRepository4

    def setup() {
        runBlocking { cont ->
            def entity = new TestEntity()
            entity.id = ENTITY_ID
            entity.name = "Hello"
            entity.number = 17
            testRepository.insert(entity, cont)
        }
    }

    def cleanup() {
        runBlocking { cont ->
            testRepository.deleteAll(cont)
        }
    }

    def "should find list of entities by name"() {
        when:
        def result = runBlocking { cont ->
            testRepository.findByName("Hello", cont)
        }

        then:
        result.size() == 1
    }

    def "should find a single entity"() {
        when:
        def result = runBlocking { cont ->
            testRepository.findByNumber(17, cont)
        }

        then:
        result.number == 17
    }

    @Unroll
    @Ignore
    def "should not find non-existing entity"() {
        when:
        def result = runBlocking { cont ->
            testRepository.findByNumber(18, cont)
        }

        then:
        result == null
    }

    def "should find list of entities by name with ReceiveChannel"() {
        when:
        def result = runBlocking { cont ->
            testRepository2.findByName("Hello", cont)
        }

        then:
        asList(result).size() == 1
    }

    def "should find list of entities by name with ReceiveChannel via regular function"() {
        when:
        def result = testRepository3.findByName("Hello")

        then:
        asList(result).size() == 1
    }


    def "should find a single entity via regular function"() {
        when:
        def result = testRepository3.findByNumber(17)

        then:
        result.number == 17
    }

    def "should not find non-existing entity via regular function"() {
        when:
        def result = testRepository3.findByNumber(18)

        then:
        result == null
    }


    def "should find list of entities by name via regular function"() {
        when:
        def result = testRepository4.findByName("Hello")

        then:
        result.size() == 1
    }

    @Ignore
    @Unroll
    def "should find entity by id"() {
        when:
        def result = runBlocking { cont ->
            testRepository.findById(entityId, cont)
        }

        then:
        (result == null) ^ resultExists

        where:
        entityId         | resultExists
        ENTITY_ID        | true
        "abcd"           | false
    }

    @Unroll
    def "should check if entity exists by id"() {
        when:
        def result = runBlocking { cont ->
            testRepository.existsById(entityId, cont)
        }

        then:
        result == resultExists

        where:
        entityId         | resultExists
        ENTITY_ID        | true
        "abcd"           | false
    }

    def "should delete entities by collection"() {
        given:
        def entity2 = new TestEntity()
        entity2.id = "id-2"
        def entity3 = new TestEntity()
        entity3.id = "id-3"
        runBlocking { cont ->
            testRepository.saveAll([entity2, entity3], cont)
        }

        when:
        runBlocking { cont ->
            testRepository.deleteAll([entity3], cont)
        }

        then:
        def result = runBlocking { cont ->
            testRepository.findAll(cont)
        }

        result.size() ==  2
    }
}
