/*
 * Copyright 2019-2020 the original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.hauner.openapi.spring.converter.schema

import com.github.hauner.openapi.spring.converter.mapping.Mapping

import static com.github.hauner.openapi.spring.converter.mapping.Mapping.Level.*


interface SchemaType {

    List<Mapping> matchEndpointMapping (List<Mapping> typeMappings)
    List<Mapping> matchIoMapping (List<Mapping> typeMappings)
    List<Mapping> matchTypeMapping (List<Mapping> typeMappings)

}

abstract class BaseSchemaType implements SchemaType {

    protected SchemaInfo info

    BaseSchemaType (SchemaInfo info) {
        this.info = info
    }

    @Override
    List<Mapping> matchEndpointMapping (List<Mapping> typeMappings) {
        // mappings matching by path
        List<Mapping> endpoint = typeMappings.findAll {
            it.isLevel (ENDPOINT) && it.matches (info)
        }.collect {
            it.childMappings
        }.flatten () as List<Mapping>

        // io mappings
        List<Mapping> io = endpoint.findAll {
            it.isLevel (IO) && it.matches (info)
        }.collect {
            it.childMappings
        }.flatten () as List<Mapping>

        if (!io.empty) {
            return io
        }

        // type mappings
        matchTypeMapping (endpoint)
    }

    List<Mapping> matchIoMapping (List<Mapping> typeMappings) {
        // io mappings
        typeMappings.findAll {
            it.isLevel (IO) && it.matches (info)
        }.collect {
            it.childMappings
        }.flatten () as List<Mapping>
    }

}

class ObjectSchemaType extends BaseSchemaType {

    ObjectSchemaType (SchemaInfo info) {
        super (info)
    }

    @Override
    List<Mapping> matchTypeMapping (List<Mapping> typeMappings) {
        typeMappings.findAll {
            it.isLevel (TYPE) && it.matches (info)
        }
    }

}

class ArraySchemaType extends BaseSchemaType {

    ArraySchemaType (SchemaInfo info) {
        super (info)
    }

    @Override
    List<Mapping> matchTypeMapping (List<Mapping> typeMappings) {
        def array = new SchemaInfo (name: 'array')
        typeMappings.findAll () {
            it.isLevel (TYPE) && it.matches (array)
        }
    }

}

class PrimitiveSchemaType extends BaseSchemaType {

    PrimitiveSchemaType (SchemaInfo info) {
        super (info)
    }

    @Override
    List<Mapping> matchTypeMapping (List<Mapping> typeMappings) {
        typeMappings.findAll () {
            (it.isLevel (TYPE)
                // simple but ignores the interface!
                && it.sourceTypeName == info.type
                && it.sourceTypeFormat == info.format)
        }
    }

}
