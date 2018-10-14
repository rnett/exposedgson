package com.rnett.exposedgson

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class JsonName(val name: String)

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class JsonIgnore

@Target(AnnotationTarget.PROPERTY)
annotation class JsonUseAsID

@Target(AnnotationTarget.CLASS)
annotation class JsonDatabaseIdField(val fieldName: String)