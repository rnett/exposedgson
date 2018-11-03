package com.rnett.exposedgson

annotation class ExposedGSON {

    /**
     * Changes the name of the target property in the JSON to [name].
     */
    @Target(AnnotationTarget.PROPERTY)
    @MustBeDocumented
    annotation class JsonName(val name: String)

    /**
     * Does not include the target property in the JSON.
     */
    @Target(AnnotationTarget.PROPERTY)
    @MustBeDocumented
    annotation class Ignore

    /**
     * Uses the target property as the database id instead of creating a special field in the JSON.
     *
     * **The value of the property will be passed to findByID to create the object, make sure this will work as intended.**
     *
     * May only be used once per class.
     *
     * Will throw an exception if @JsonDatabaseIdField is also present.
     */
    @Target(AnnotationTarget.PROPERTY)
    annotation class UseAsID

    /**
     * Uses [fieldName] as the name of special id field in the JSON instead of the default ($$database_id$$).
     *
     * Will throw an exception if @UseAsID is also present.
     */
    @Target(AnnotationTarget.CLASS)
    annotation class JsonDatabaseIdField(val fieldName: String)
}