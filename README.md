# Exposed GSON

Gson type adapters for [Kotlin Exposed](https://github.com/JetBrains/Exposed).
For use with Exposed DAO.

## Warning
This is somewhat hacky, use at your own risk (reflection and casting).
It should work properly on valid DAO classes.

Issues and improvements are welcome.

## Usage
To use with a Exposed DAO class, add the annotation `@JsonAdapter(ExposedTypeAdapter::class)`.
E.x.:
```kotlin
@JsonAdapter(ExposedTypeAdapter::class)
class account(id: EntityID<Int>) : IntEntity(id){
```

Then, if you use `Gson().toJson()` (or any other serialization methods) on an instance of your entity,
it will be serialized using Exposed GSON's type adapter.

The JSON will only include declared fields, along with a database id.

By default, this id is the `id` property of the `Entity` superclass, and is stored in the JSON field `$$database_id$$`.
However, these are both changeable; see `@JsonUseAsID` and `@JsonDatabaseIdField`.

**When parsing JSON, Exposed GSON only looks at the database id, then pulls the entity from the database.**

Changes made in the JSON will not be reflected in the parsed object or in the database (this is something I may add in the future).

## Annotations

Exposed GSON provides a number of annotations to customize the JSON representation of the entity.
These are `@JsonName`, `@JsonIgnore`, `@JsonUseAsID`, and `@JsonDatabaseIdField`.

### @JsonName

`@JsonName` can be applied to properties.
It takes a string as a parameter, and uses that string as the property name in the JSON.
E.x.:
```kotlin
@JsonName("this_email")
var email by accounts.email
```
`email` will then be represented by the JSON field `this_email`.

### @JsonIgnore

`@JsonIgnore` can be applied to properties.
It is simple: if it is present, the property will not be included in the JSON.
E.x.:
```kotlin
@JsonIgnore
var pwHash by accounts.pwHash
```
`pwHash` will then not be included in the JSON.

### @JsonUseAsID

`@JsonUseAsID` can be applied to properties (only one per class).
It tells Exposed GSON to use that property as the entity id when creating the object.

**When parsing JSON, Exposed GSON will pass the value of this property to the entity class's `findById` method to generate the entity from the database.**
Make sure this is the intended behavior.

When this annotation is present, Exposed GSON will not create a separate database id field in the JSON.

If this annotation is present more than once in a class, an exception will be thrown.

This annotation will work with `@JsonName`.

E.x.:
```kotlin
object accounts : IntIdTable("accounts", "accountid") {

    // Database Columns

    val accountid = integer("accountid").autoIncrement().primaryKey()

    ...
}

@JsonAdapter(ExposedTypeAdapter::class)
class account(id: EntityID<Int>) : IntEntity(id) {
    @JsonUseAsID
    val accountid by accounts.accountid

    ...
}
```

`accountid` will then be used in place of the special id field in the JSON.

Note that `accountid` is the primary key of the table, and is the correct type for `findById`

### @JsonDatabaseIdField
`@JsonDatabaseIdField` can be applied to the DAO class.
It is meaningless unless `@JsonAdapter(ExposedTypeAdapter::class)` is also used.

`@JsonDatabaseIdField` takes a string, and uses that string as the name of the special id field in the JSON (instead of `$$database_id$$`).

If both `@JsonDatabaseIdField` and `@JsonUseAsID` are present, an exception will be thrown.

E.x.:
```kotlin
@JsonAdapter(ExposedTypeAdapter::class)
@JsonDatabaseIdField("this_id")
class account(id: EntityID<Int>) : IntEntity(id) {
    ...
}
```
Then `this_id` will be used as the id field in the JSON, instead of `$$database_id$$`.