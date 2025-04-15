package edu.sergiosoria.valhallathebox.database

import androidx.room.*
import edu.sergiosoria.valhallathebox.models.Product

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: Product)

    @Query("SELECT * FROM products")
    fun getAllProducts(): List<Product>
}