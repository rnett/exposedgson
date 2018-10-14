package com.rnett.exposedgson

import com.google.gson.annotations.JsonAdapter
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

enum class AccountType{
    Customer, Employee, Admin;
}

object accounts : IntIdTable("accounts", "accountid") {

    // Database Columns

    val accountid = integer("accountid").autoIncrement().primaryKey()
    val email = varchar("email", 100)
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

    @JsonUseAsID
    val accountid by accounts.accountid

    @JsonName("this_email")
    var email by accounts.email

    @JsonIgnore
    var pwhash by accounts.pwhash
    var type by accounts.type


    // Helper Methods

    override fun equals(other: Any?): Boolean {
        if(other == null || other !is account)
            return false

        return accountid == other.accountid
    }

    override fun hashCode(): Int = accountid
}