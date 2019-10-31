/*
 * Copyright 2019 the original authors
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

package com.github.hauner.openapi.spring.model.datatypes

/**
 * OpenAPI type 'array' maps to Collection<>.
 *
 * @author Martin Hauner
 */
class CollectionDataType implements DataType {

    private DataType item

    @Override
    String getName () {
        "Collection<${item.name}>"
    }

    @Override
    String getPackageName () {
        'java.util'
    }

    @Override
    String getImport () {
        [packageName, 'Collection'].join('.')
    }

    @Override
    Set<String> getImports () {
        [getImport ()] + item.imports
    }

}