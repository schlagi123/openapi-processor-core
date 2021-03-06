/*
 * This class is auto generated by https://github.com/hauner/openapi-processor-core.
 * TEST ONLY.
 */

package generated.api;

import annotation.Mapping;

public interface Api {

    @Mapping("/string")
    String getString();

    @Mapping("/integer")
    Integer getInteger();

    @Mapping("/long")
    Long getLong();

    @Mapping("/float")
    Float getFloat();

    @Mapping("/double")
    Double getDouble();

    @Mapping("/boolean")
    Boolean getBoolean();

    @Mapping("/array-string")
    String[] getArrayString();

}
