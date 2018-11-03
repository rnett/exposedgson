package com.rnett.exposedgson.database

import com.google.gson.annotations.JsonAdapter
import com.rnett.exposedgson.ExposedGSON
import com.rnett.exposedgson.ExposedTypeAdapter
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object billinginfos : IntIdTable("billinginfo", "id") {

    // Database Columns

    val idCol = integer("id").primaryKey().autoIncrement()
    val accountid = integer("accountid")
    val streetaddress = varchar("streetaddress", 200)
    val zip = integer("zip")
    val state = varchar("state", 2)
    val city = varchar("city", 200)
    val ccnumber = integer("ccnumber")

    val account = reference("accountid", accounts)

}

@JsonAdapter(ExposedTypeAdapter::class)
class billinginfo(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<billinginfo>(billinginfos) {
        fun idFromPKs(idCol: Int): Int = idCol

        fun findByPKs(idCol: Int) = findById(idFromPKs(idCol))
    }

    // Database Columns

    @ExposedGSON.UseAsID
    val idCol by billinginfos.idCol
    var accountid by billinginfos.accountid
    var streetaddress by billinginfos.streetaddress
    var zip by billinginfos.zip
    var state by billinginfos.state
    var city by billinginfos.city
    var ccnumber by billinginfos.ccnumber

    @ExposedGSON.Ignore
    var account by com.rnett.exposedgson.database.account referencedOn billinginfos.account


    // Helper Methods

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is employee)
            return false

        return idCol == other.idCol
    }

    override fun hashCode(): Int = idCol
}