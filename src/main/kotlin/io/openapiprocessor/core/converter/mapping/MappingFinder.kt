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

package io.openapiprocessor.core.converter.mapping

import io.openapiprocessor.core.converter.SchemaInfo

/**
 * find mapping in type mapping list for a schema info.
 *
 * @author Martin Hauner
 */
class MappingFinder(
    private val typeMappings: List<Mapping> = emptyList()
) {

    /**
     * find any matching endpoint mapping for the given schema info.
     *
     * @param info schema info of the OpenAPI schema.
     * @return list of matching mappings
     */
    fun findEndpointMappings(info: SchemaInfo): List<Mapping> {
        val ep = filterMappings(EndpointMatcher(info), typeMappings)

        val io = filterMappings(IoMatcher(info), ep)
        if (io.isNotEmpty()) {
            return io
        }

        return filterMappings (TypeMatcher(info), ep)
    }

    /**
     * find any matching (global) io mapping for the given schema info.
     *
     * @param info schema info of the OpenAPI schema.
     * @return list of matching mappings
     */
    fun findIoMappings(info: SchemaInfo): List<Mapping> {
        return filterMappings(IoMatcher(info), typeMappings)
    }

    /**
     * find any matching (global) type mapping for the given schema info.
     *
     * @param info schema info of the OpenAPI schema.
     * @return list of matching mappings
     */
    fun findTypeMappings(info: SchemaInfo): List<Mapping> {
        return filterMappings (TypeMatcher(info), typeMappings)
    }

    /**
     * find additional parameter mappings for the given endpoint.
     *
     * @param path the endpoint path
     * @return list of matching mappings
     */
    fun findAdditionalEndpointParameter(path: String): List<Mapping> {
        val info = MappingSchemaEndpoint(path)
        val ep = filterMappings(EndpointMatcher(info), typeMappings)

        val matcher = AddParameterMatcher(info)
        val add = ep.filter {
            it.matches (matcher)
        }

        if (add.isNotEmpty()) {
            return add
        }

        return emptyList()
    }

    /**
     * find endpoint result type mapping.
     *
     * @param info schema info of the OpenAPI schema.
     * @return the result type mapping.
     */
    fun findEndpointResultMapping(info: SchemaInfo): List<Mapping> {
        val ep = filterMappings(EndpointMatcher(info), typeMappings)

        val matcher = ResultTypeMatcher(info)
        val result = ep.filter {
            it.matches (matcher)
        }

        if (result.isNotEmpty()) {
            return result
        }

        return emptyList()
    }

    /**
     * find (global) result type mapping.
     *
     * @param info schema info of the OpenAPI schema.
     * @return the result type mapping.
     */
    fun findResultMapping(info: SchemaInfo): List<Mapping> {
        val ep = filterMappings(ResultTypeMatcher(info), typeMappings)
        if (ep.isNotEmpty()) {
            return ep
        }

        return emptyList()
    }

    /**
     * find endpoint single type mapping.
     *
     * @param info schema info of the OpenAPI schema.
     * @return the single type mapping.
     */
    fun findEndpointSingleMapping(info: SchemaInfo): List<Mapping> {
        val ep = filterMappings(EndpointMatcher(info), typeMappings)

        val matcher = SingleTypeMatcher(info)
        val result = ep.filter {
            it.matches(matcher)
        }

        if (result.isNotEmpty()) {
            return result
        }

        return emptyList()
    }

    /**
     * find (global) single type mapping.
     *
     * @param info schema info of the OpenAPI schema.
     * @return the single type mapping.
     */
    fun findSingleMapping(info: SchemaInfo): List<Mapping> {
        val ep = filterMappings(SingleTypeMatcher(info), typeMappings)
        if (ep.isNotEmpty()) {
            return ep
        }

        return emptyList()
    }

    /**
     * find endpoint multi type mapping.
     *
     * @param info schema info of the OpenAPI schema.
     * @return the multi type mapping.
     */
    fun findEndpointMultiMapping(info: SchemaInfo): List<Mapping> {
        val ep = filterMappings(EndpointMatcher(info), typeMappings)

        val matcher = MultiTypeMatcher(info)
        val result = ep.filter {
            it.matches (matcher)
        }

        if (result.isNotEmpty()) {
            return result
        }

        return emptyList()
    }

    /**
     * find (global) multi type mapping.
     *
     * @param info schema info of the OpenAPI schema.
     * @return the multi type mapping.
     */
    fun findMultiMapping(info: SchemaInfo): List<Mapping> {
        val ep = filterMappings(MultiTypeMatcher(info), typeMappings)
        if (ep.isNotEmpty()) {
            return ep
        }

        return emptyList()
    }

    /**
     * check if the given endpoint should b excluded.
     *
     * @param path the endpoint path
     * @return true/false
     */
    fun isExcludedEndpoint(path: String): Boolean {
        val info = MappingSchemaEndpoint(path)
        val matcher = EndpointMatcher(info)

        val ep = typeMappings.filter {
            it.matches (matcher)
        }

        if (ep.isNotEmpty()) {
            if (ep.size != 1) {
                throw AmbiguousTypeMappingException(ep.map { it as TypeMapping })
            }

            val match = ep.first () as EndpointTypeMapping
            return match.exclude
        }

        return false
    }

    private fun filterMappings(visitor: MappingVisitor, mappings: List<Mapping>): List<Mapping> {
        return mappings
            .filter { it.matches(visitor) }
            .map { it.getChildMappings() }
            .flatten()
    }

}

class MappingSchemaEndpoint(private val path: String): MappingSchema {

    override fun getPath(): String {
        return path
    }

    override fun getName(): String {
        throw NotImplementedError() // return "" // null
    }

    override fun getContentType(): String {
        throw NotImplementedError() // return "" // null
    }

    override fun getType(): String {
        throw NotImplementedError() // return "" // null
    }

    override fun getFormat(): String? {
        throw NotImplementedError()// return null
    }

    override fun isPrimitive(): Boolean {
        throw NotImplementedError()
    }

    override fun isArray(): Boolean {
        throw NotImplementedError()
    }

}

class EndpointMatcher(schema: MappingSchema): BaseVisitor(schema) {

    override fun match(mapping: EndpointTypeMapping): Boolean {
        return mapping.path == schema.getPath()
    }

}

class IoMatcher(schema: MappingSchema): BaseVisitor(schema) {

    override fun match(mapping: ParameterTypeMapping): Boolean {
        return mapping.parameterName == schema.getName()
    }

    override fun match(mapping: ResponseTypeMapping): Boolean {
        return mapping.contentType == schema.getContentType()
    }

}

class TypeMatcher(schema: MappingSchema): BaseVisitor(schema) {

    override fun match(mapping: TypeMapping): Boolean {
        // try to match by name first, the format must match to avoid matching primitive
        // and primitive with format e.g. string should not match string:binary
        if (matchesName(mapping) && matchesFormat(mapping)) {
            return true
        }

        return when {
            schema.isPrimitive() -> {
                matchesType(mapping) && matchesFormat(mapping)
            }
            schema.isArray() -> {
                matchesArray(mapping)
            }
            else -> {
                // nop
                false
            }
        }
    }

    private fun matchesName(mapping: TypeMapping): Boolean = mapping.sourceTypeName == schema.getName()
    private fun matchesType(mapping: TypeMapping): Boolean = mapping.sourceTypeName == schema.getType()
    private fun matchesArray(mapping: TypeMapping): Boolean = mapping.sourceTypeName == "array"
    private fun matchesFormat(mapping: TypeMapping): Boolean = mapping.sourceTypeFormat == schema.getFormat()

}

class ResultTypeMatcher(schema: MappingSchema): BaseVisitor(schema) {

    override fun match(mapping: ResultTypeMapping): Boolean {
        return true
    }

}

class SingleTypeMatcher(schema: MappingSchema): BaseVisitor(schema) {

    override fun match(mapping: TypeMapping): Boolean {
        return mapping.sourceTypeName == "single"
    }

}

class MultiTypeMatcher(schema: MappingSchema): BaseVisitor(schema) {

    override fun match(mapping: TypeMapping): Boolean {
        return mapping.sourceTypeName == "multi"
    }

}

class AddParameterMatcher(schema: MappingSchema): BaseVisitor(schema) {

    override fun match(mapping: AddParameterTypeMapping): Boolean {
        return true
    }

}

open class BaseVisitor(protected val schema: MappingSchema): MappingVisitor {

    override fun match(mapping: EndpointTypeMapping): Boolean {
        return false
    }

    override fun match(mapping: ParameterTypeMapping): Boolean {
        return false
    }

    override fun match(mapping: ResponseTypeMapping): Boolean {
        return false
    }

    override fun match(mapping: TypeMapping): Boolean {
        return false
    }

    override fun match(mapping: AddParameterTypeMapping): Boolean {
        return false
    }

    override fun match(mapping: ResultTypeMapping): Boolean {
        return false
    }

}
