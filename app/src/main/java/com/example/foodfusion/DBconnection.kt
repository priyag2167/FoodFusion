package com.example.foodfusion


import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DBConnection {
    private const val IP = "10.0.2.2"
    private const val PORT = "1433"
    private const val DATABASE_NAME = "HotelHaven"
    private const val USERNAME = "sa"
    private const val PASSWORD = "12345678"
    private const val URL = "jdbc:jtds:sqlserver://$IP:$PORT/$DATABASE_NAME"

     fun connect(): Connection? {
        return try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver")
                DriverManager.getConnection(URL, USERNAME, PASSWORD)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                null
            } catch (e: SQLException) {
                e.printStackTrace()
                null
            }
    }
}
