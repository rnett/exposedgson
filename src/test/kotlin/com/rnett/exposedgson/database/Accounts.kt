package com.rnett.exposedgson.database

import com.google.gson.annotations.JsonAdapter
import com.rnett.exposedgson.ExposedGSON
import com.rnett.exposedgson.ExposedTypeAdapter
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object employees : IntIdTable("employees", "id") {

    // Database Columns

    val idCol = integer("id").primaryKey().autoIncrement()
    val email = varchar("email", 100).uniqueIndex()
    val dateAdded = datetime("dateadded")
}

@JsonAdapter(ExposedTypeAdapter::class)
class employee(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<employee>(employees) {
        fun idFromPKs(idCol: Int): Int = idCol

        fun findByPKs(idCol: Int) = findById(idFromPKs(idCol))
    }

    // Database Columns

    @ExposedGSON.UseAsID
    val idCol by employees.idCol
    var email by employees.email
    var dateAdded by employees.dateAdded


    // Helper Methods

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is employee)
            return false

        return idCol == other.idCol
    }

    override fun hashCode(): Int = idCol
}

enum class AccountType {
    Customer, Employee, Admin;
}

object accounts : IntIdTable("accounts", "accountid") {

    // Database Columns

    val accountid = integer("accountid").autoIncrement().primaryKey()
    val email = varchar("email", 100).uniqueIndex()
    val pwhash = varchar("pwhash", 1000)
    val type = enumeration("type", AccountType::class)
}

@JsonAdapter(ExposedTypeAdapter::class)
class account(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<account>(accounts) {
        fun idFromPKs(accountid: Int): Int = accountid

        fun findByPKs(accountid: Int) = findById(idFromPKs(accountid))
    }

    // Database Columns

    @ExposedGSON.UseAsID
    val accountid by accounts.accountid
    var email by accounts.email
    @ExposedGSON.Ignore
    var pwhash by accounts.pwhash
    var type by accounts.type


    // Referencing Keys
    val billinginfos by billinginfo referrersOn com.rnett.exposedgson.database.billinginfos.account

    // Helper Methods

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is account)
            return false

        return accountid == other.accountid
    }

    override fun hashCode(): Int = accountid
}

object apitokens : IntIdTable("apitokens", "id") {

    // Database Columns
    val idCol = integer("id").autoIncrement().primaryKey()
    val accountid = integer("accountid")
    val token = varchar("token", 500)

    // Foreign/Imported Keys (One to Many)

    val account = reference("accountid", accounts)

    init {
        uniqueIndex(accountid, token)
    }
}


@JsonAdapter(ExposedTypeAdapter::class)
class apitoken(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<apitoken>(apitokens) {
        fun idFromPKs(id: Int): Int = id

        fun findByPKs(id: Int) = findById(idFromPKs(id))
    }

    // Database Columns

    @ExposedGSON.UseAsID
    val idCol by apitokens.idCol
    var accountid by apitokens.accountid
    var token by apitokens.token

    // Foreign/Imported Keys (One to Many)

    var account by com.rnett.exposedgson.database.account referencedOn apitokens.account

    // Helper Methods

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is apitoken)
            return false

        return idCol == other.idCol
    }


    override fun hashCode(): Int = idCol

}