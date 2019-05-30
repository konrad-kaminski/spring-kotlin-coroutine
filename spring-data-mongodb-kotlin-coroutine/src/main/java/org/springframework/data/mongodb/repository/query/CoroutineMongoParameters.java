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

package org.springframework.data.mongodb.repository.query;

import kotlin.coroutines.Continuation;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class CoroutineMongoParameters extends MongoParameters {
    public CoroutineMongoParameters(Method method, boolean isGeoNearMethod) {
        super(method, isGeoNearMethod);
    }

    @Override
    protected MongoParameter createParameter(MethodParameter parameter) {
        if (parameter.getParameterType() == Continuation.class) {
            parameter.initParameterNameDiscovery(new ParameterNameDiscoverer() {
                @Nullable
                @Override
                public String[] getParameterNames(Method method) {
                    final String[] names = new String[parameter.getParameterIndex() + 1];
                    names[parameter.getParameterIndex()] = "__continuation__";
                    return names;
                }

                @Nullable
                @Override
                public String[] getParameterNames(Constructor<?> ctor) {
                    return new String[0];
                }
            });
        }

        return super.createParameter(parameter);
    }
}
