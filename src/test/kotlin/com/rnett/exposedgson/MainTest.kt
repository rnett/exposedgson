package com.rnett.exposedgson

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.apache.commons.dbcp.BasicDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object PooledDBConnection {
    fun connect(connectionURL: String): BasicDataSource {
        val ds = BasicDataSource()
        ds.url = connectionURL
        return ds
    }
}
fun connectToDB(){
    Database.connect(PooledDBConnection.connect(PooledDBConnection::class.java.getResource("/database.txt").readText().trim()))
}
fun main(args: Array<String>) {
    connectToDB()

    val account = transaction{account.all().first()}

    val gson = transaction{ Gson().toJson(account)}
    println(gson)
    val account2 = transaction{Gson().fromJson<account>(gson)}

    println("Equal: ${account == account2}")
}