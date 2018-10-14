package com.rnett.exposedgson

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

/**
 * The default name of the special id field in the JSON.
 */
const val TABLE_ID_NAME = "\$\$database_id\$\$"

/**
 * The Exposed TypeAdapter Factory
 */
class ExposedTypeAdapter : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        // if this is not a DAO class, don't provide a TypeAdapter
        if(!Entity::class.isSuperclassOf((type.rawType as Class<Any>).kotlin))
            return null

        return object : TypeAdapter<T>() {

            // The class of the DAO
            val klass = (type.rawType as Class<Any>).kotlin

            // The properties to be included in the JSON
            val properties = klass.declaredMemberProperties.filter{ it.findAnnotation<Transient>() == null && it.findAnnotation<JsonIgnore>() == null }

            // The id property (from Entity)
            val id = klass.memberProperties.first { it.name == "id" }

            // the custom name for the database id field, or default
            val customDBIdName = run {
                val name = klass.findAnnotation<JsonDatabaseIdField>()?.fieldName ?: TABLE_ID_NAME
                if(name in properties.map { propertyName((it)) })
                    if(name == TABLE_ID_NAME)
                        throw IllegalArgumentException("Property can not have name of default database id: $TABLE_ID_NAME")
                    else
                        TABLE_ID_NAME
                else
                    name
            }

            // the custom column to use as the database id
            val customId : KProperty1<Any, *>?
            init{
                val ids = klass.declaredMemberProperties.filter{ it.findAnnotation<JsonUseAsID>() != null }
                if(ids.count() > 1)
                    throw IllegalArgumentException("More than one column specified as the database id (with @JsonUseAsID)")
                else if(ids.count() > 0 && customDBIdName != TABLE_ID_NAME)
                    throw IllegalArgumentException("Can not specify Database ID Name (with @JsonDatabaseIdField) and a database id column (with @JsonUseAsID)")
                else if(ids.count() == 1 && ids.first().findAnnotation<JsonIgnore>() != null)
                    throw IllegalArgumentException("Can not use @JsonUseAsID and @JsonIgnore on the same field.")
                else if(ids.count() == 1)
                    customId = ids.first()
                else
                    customId = null
            }

            // the name of the database id field
            val dbIdName = customId?.let(::propertyName) ?: customDBIdName

            // whether to create a special id field
            val useSpecialDbId = customId == null

            // the companion object(an EntityClass)
            val entityCompanion = klass.companionObject!!

            // the findById function in the companion.
            val findById = entityCompanion.memberFunctions.first{ it.name == "findById" && "EntityID" !in it.valueParameters.first{ it.index == 1 }.type.toString() }

            // get the name of a property, using @JsonName if present
            fun propertyName(prop: KProperty1<Any, *>) = prop.findAnnotation<JsonName>()?.name ?: prop.name

            override fun write(out: JsonWriter, value: T) {
                out.beginObject()

                // create the special id column if nessecary
                if(useSpecialDbId) {
                    out.name(dbIdName)

                    // writes the value of the id, not the id itself (the id also contains the table and other stuff)
                    out.value(gson.toJson((id.get(value as Any) as EntityID<*>).value))
                }

                // add properties
                properties.forEach {
                    out.name(propertyName(it))

                    out.jsonValue(gson.toJson(it.get(value as Any)))
                }

                out.endObject()
            }

            override fun read(input: JsonReader): T? {
                input.beginObject()

                // the entity object
                var entity: Entity<*>? = null

                while(input.hasNext())
                    when(input.nextName()){
                        dbIdName -> run{

                            // if the field is the database id

                            val id = gson.fromJson(input.nextString(), Any::class.java)

                            // call the findById function with the value of the id
                            entity = findById.call(klass.companionObjectInstance as EntityClass<*, *>, id) as Entity<*>?
                        }
                        else -> input.skipValue()
                    }

                input.endObject()

                //TODO update fields from json

                return entity as T?
            }
        }
    }
}
