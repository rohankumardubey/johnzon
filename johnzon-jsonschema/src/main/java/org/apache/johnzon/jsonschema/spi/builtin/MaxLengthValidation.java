/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.johnzon.jsonschema.spi.builtin;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.johnzon.jsonschema.ValidationResult;
import org.apache.johnzon.jsonschema.spi.ValidationContext;
import org.apache.johnzon.jsonschema.spi.ValidationExtension;

public class MaxLengthValidation implements ValidationExtension {
    @Override
    public Optional<Function<JsonValue, Stream<ValidationResult.ValidationError>>> create(final ValidationContext model) {
        if (model.getSchema().getString("type", "object").equals("string")) {
            return Optional.ofNullable(model.getSchema().get("maxLength"))
                    .filter(v -> v.getValueType() == JsonValue.ValueType.NUMBER)
                    .map(m -> new Impl(model.toPointer(), model.getValueProvider(), JsonNumber.class.cast(m).intValue()));
        }
        return Optional.empty();
    }

    private static class Impl extends BaseValidation {
        private final int bound;

        private Impl(final String pointer, final Function<JsonValue, JsonValue> valueProvider, final int bound) {
            super(pointer, valueProvider, JsonValue.ValueType.STRING);
            this.bound = bound;
        }

        @Override
        protected Stream<ValidationResult.ValidationError> onString(final JsonString val) {
            if (val.getString().length() > bound) {
                return Stream.of(new ValidationResult.ValidationError(pointer, val + " length is more than " + this.bound));
            }
            return Stream.empty();
        }

        @Override
        public String toString() {
            return "MaxLength{" +
                    "factor=" + bound +
                    ", pointer='" + pointer + '\'' +
                    '}';
        }
    }
}
