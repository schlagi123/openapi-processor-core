/*
 * Copyright 2020 the original authors
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

package io.openapiprocessor.core.converter.wrapper

import io.openapiprocessor.core.converter.ApiOptions
import io.openapiprocessor.core.converter.SchemaInfo
import io.openapiprocessor.core.converter.mapping.*
import io.openapiprocessor.core.model.datatypes.ArrayDataType
import io.openapiprocessor.core.model.datatypes.DataType
import io.openapiprocessor.core.model.datatypes.MappedCollectionDataType
import io.openapiprocessor.core.model.datatypes.NoneDataType

/**
 * replaces a collection wrapper with the 'multi' data mapping.
 *
 * Used to replace the collection wrapper at Responses or RequestBody's with  {@code Flux<>} or
 * similar types.
 *
 * @author Martin Hauner
 */
class MultiDataTypeWrapper(
    private val options: ApiOptions,
    private val finder: MappingFinder = MappingFinder(options.typeMappings)
) {

    /**
     * replaces an (converted) array data type with a multi data type (like {@code Flux< >})
     * wrapping the collection item.
     *
     * If the configuration for the result type is 'plain' or not defined the source data type
     * is not changed.
     *
     * @param dataType the data type to wrap
     * @param schemaInfo the open api type with context information
     * @return the resulting java data type
     */
    fun wrap(dataType: DataType, schemaInfo: SchemaInfo): DataType {
        if (!schemaInfo.isArray()) {
            return dataType
        }

        val targetType = getMultiDataType(schemaInfo)
        if (targetType == null) {
            return dataType
        }

        if (targetType.typeName == "plain") {
            return dataType
        }

        return MappedCollectionDataType(
            targetType.getName(),
            targetType.getPkg(),
            (dataType as ArrayDataType).item,
            null,
            false
        )
    }

    private fun getMultiDataType(info: SchemaInfo): TargetType? {
        // check endpoint multi mapping
        val endpointMatches = finder.findEndpointMultiMapping(info)

        if (endpointMatches.isNotEmpty()) {

            if (endpointMatches.size != 1) {
                throw AmbiguousTypeMappingException(endpointMatches.toTypeMapping())
            }

            val target = (endpointMatches.first() as TargetTypeMapping).getTargetType()
            if (target != null) {
                return target
            }
        }

        // find global multi mapping
        val typeMatches = finder.findMultiMapping(info)
        if (typeMatches.isEmpty ()) {
            return null
        }

        if (typeMatches.size != 1) {
            throw AmbiguousTypeMappingException(typeMatches.toTypeMapping())
        }

        val match = typeMatches.first () as TargetTypeMapping
        return match.getTargetType()
    }

    private fun checkNone(dataType: DataType): DataType {
        if (dataType is NoneDataType) {
            return dataType.wrappedInResult ()
        }
        return dataType
    }

}
