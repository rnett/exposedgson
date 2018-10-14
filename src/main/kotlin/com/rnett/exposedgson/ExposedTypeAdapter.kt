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

private const val TABLE_ID_NAME = "\$\$database_id\$\$"


class ExposedTypeAdapter : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        if(!Entity::class.isSuperclassOf((type.rawType as Class<Any>).kotlin))
            return null

        return object : TypeAdapter<T>() {

            val klass = (type.rawType as Class<Any>).kotlin
            val params = klass.declaredMemberProperties.filter{ it.findAnnotation<Transient>() == null && it.findAnnotation<JsonIgnore>() == null }
            val id = klass.memberProperties.first { it.name == "id" }

            val customDBIdName = run {
                val name = klass.findAnnotation<JsonDatabaseIdField>()?.fieldName ?: TABLE_ID_NAME
                if(name in params.map { propertyName((it)) })
                    if(name == TABLE_ID_NAME)
                        throw IllegalArgumentException("Property can not have name of default database id: $TABLE_ID_NAME")
                    else
                        TABLE_ID_NAME
                else
                    name
            }

            val customId : KProperty1<Any, *>?
            init{
                val ids = klass.declaredMemberProperties.filter{ it.findAnnotation<JsonUseAsID>() != null }
                if(ids.count() > 1)
                    throw IllegalArgumentException("More than one column specified as the database id (with JsonUseAsID)")
                else if(ids.count() > 0 && customDBIdName != TABLE_ID_NAME)
                    throw IllegalArgumentException("Can not specify Database ID Name (with JsonDatabaseIdField) and a database id column (with JsonUseAsID)")
                else if(ids.count() == 1)
                    customId = ids.first()
                else
                    customId = null
            }

            val dbIdName = customId?.let(::propertyName) ?: customDBIdName
            val useSpecialDbId = customId == null

            val comp = klass.companionObject!!
            val findByID = comp.memberFunctions.first{ it.name == "findById" && "EntityID" !in it.valueParameters.first{ it.index == 1 }.type.toString() }

            fun propertyName(prop: KProperty1<Any, *>) = prop.findAnnotation<JsonName>()?.name ?: prop.name

            override fun write(out: JsonWriter, value: T) {
                out.beginObject()

                if(useSpecialDbId) {
                    out.name(dbIdName)
                    out.value(gson.toJson((id.get(value as Any) as EntityID<*>).value))
                }

                params.forEach {
                    out.name(propertyName(it))

                    out.jsonValue(gson.toJson(it.get(value as Any)))
                }

                out.endObject()
            }

            override fun read(input: JsonReader): T? {
                input.beginObject()

                var entity: Entity<*>? = null

                while(input.hasNext())
                    when(input.nextName()){
                        dbIdName -> run{
                            val id = gson.fromJson(input.nextString(), Any::class.java)
                            entity = findByID.call(klass.companionObjectInstance as EntityClass<*, *>, id) as Entity<*>?
                        }
                        else -> input.skipValue()
                    }

                input.endObject()

                return entity as T?
            }
        }
    }
}
