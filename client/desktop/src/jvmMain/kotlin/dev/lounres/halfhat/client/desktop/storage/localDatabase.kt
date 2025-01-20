package dev.lounres.halfhat.client.desktop.storage

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.lounres.halfhat.client.localStorage.sql.AppDatabase
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString


const val LOCAL_DATABASE_NAME = "test.db"

object DriverFactory {
    fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${Path(appDirs.getUserDataDir()).resolve(LOCAL_DATABASE_NAME).absolutePathString()}")
        AppDatabase.Schema.create(driver)
        return driver
    }
}

fun AppDatabase(driverFactory: DriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    val database = AppDatabase(driver)
    return database
}