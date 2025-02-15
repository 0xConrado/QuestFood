package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.quest.food.model.ProductItem
import com.quest.food.model.Category
import com.quest.food.model.FoodMenuItem

class ProductViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("categories")

    private val _products = MutableLiveData<List<ProductItem>>()
    val products: LiveData<List<ProductItem>> get() = _products

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> get() = _category

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    // O MutableLiveData para o status de administrador já está sendo utilizado
    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> get() = _isAdmin

    private val _selectedProduct = MutableLiveData<ProductItem?>()
    val selectedProduct: LiveData<ProductItem?> get() = _selectedProduct

    private val _promotionalProducts = MutableLiveData<List<ProductItem>>()  // Promoções
    val promotionalProducts: LiveData<List<ProductItem>> get() = _promotionalProducts

    private var originalProductList: List<ProductItem> = emptyList()

    init {
        checkAdminStatus()
        loadCategories()
        loadProducts()
        getPromotionalProducts()  // ✅ Carrega as promoções automaticamente
    }

    fun loadProducts() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<ProductItem>()
                for (categorySnapshot in snapshot.children) {
                    val productsSnapshot = categorySnapshot.child("products")
                    for (productSnapshot in productsSnapshot.children) {
                        val product = productSnapshot.getValue(ProductItem::class.java)
                        product?.let { productList.add(it) }
                    }
                }
                originalProductList = productList // Atualiza a lista original com os dados carregados
                _products.value = productList // Define a lista filtrada para a UI
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadProductsForCategory(categoryId: String) {
        if (categoryId.isNotEmpty()) {
            database.child(categoryId).child("products").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productList = mutableListOf<ProductItem>()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(ProductItem::class.java)
                        product?.let {
                            if (it.categoryId == categoryId) {
                                productList.add(it)
                            }
                        }
                    }
                    originalProductList = productList // Atualiza a lista original com os dados carregados
                    _products.value = productList // Define a lista filtrada para a UI
                }

                override fun onCancelled(error: DatabaseError) {
                    _products.value = emptyList()
                }
            })
        }
    }

    fun loadCategoryDetails(categoryId: String) {
        database.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val category = snapshot.getValue(Category::class.java)
                category?.let {
                    _category.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadCategories() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = mutableListOf<Category>()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    category?.let { categoryList.add(it) }
                }
                _categories.value = categoryList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ Carregar Produtos Promocionais
    fun getPromotionalProducts() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val promotionalList = mutableListOf<ProductItem>()
                for (categorySnapshot in snapshot.children) {
                    val productsSnapshot = categorySnapshot.child("products")
                    for (productSnapshot in productsSnapshot.children) {
                        val product = productSnapshot.getValue(ProductItem::class.java)
                        if (product?.isPromotion == true) {
                            promotionalList.add(product)
                        }
                    }
                }
                _promotionalProducts.value = promotionalList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addOrUpdateProduct(categoryId: String, product: ProductItem) {
        if (categoryId.isNotEmpty()) {
            val categoryRef = database.child(categoryId).child("products")

            if (product.id.isEmpty()) {
                val newProductRef = categoryRef.push()
                product.id = newProductRef.key ?: ""
                product.categoryId = categoryId
                newProductRef.setValue(product)
            } else {
                categoryRef.child(product.id).setValue(product)
                    .addOnSuccessListener {
                        loadProductsForCategory(categoryId)
                    }
                    .addOnFailureListener { error ->
                        println("Erro ao atualizar o produto: ${error.message}")
                    }
            }
        }
    }

    fun deleteProduct(categoryId: String, product: ProductItem) {
        if (categoryId.isNotEmpty() && product.id.isNotEmpty()) {
            database.child(categoryId).child("products").child(product.id).removeValue()
        }
    }

    fun getProductById(categoryId: String, productId: String) {
        if (categoryId.isNotEmpty() && productId.isNotEmpty()) {
            database.child(categoryId).child("products").child(productId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val product = snapshot.getValue(ProductItem::class.java)
                        _selectedProduct.value = product
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _selectedProduct.value = null
                    }
                })
        }
    }

    fun getProductsByCategoryIds(categoryIds: List<String>, callback: (List<FoodMenuItem>) -> Unit) {
        val productList = mutableListOf<FoodMenuItem>()

        categoryIds.forEach { categoryId ->
            database.child(categoryId).child("products").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(FoodMenuItem::class.java)
                        product?.let {
                            productList.add(it)
                        }
                    }
                    callback(productList)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
        }
    }

    fun filterProducts(query: String) {
        if (query.isEmpty()) {
            _products.value = originalProductList // Se a pesquisa estiver vazia, retorna todos os produtos
        } else {
            val filteredList = originalProductList.filter {
                it.name.contains(query, ignoreCase = true) // Filtra pelos nomes dos produtos
            }
            _products.value = filteredList // Atualiza a lista filtrada para a UI
        }
    }

    // Verificar se o usuário é admin (você já tem isso no código)
    fun checkAdminStatus() {
        _isAdmin.value = true // Aqui você pode substituir pela lógica que verifica a role do usuário
    }
}