package com.example.binm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.binm.data.Category
import com.example.binm.viewmodel.CategoryViewModel

@Composable
fun AppDrawerContent(
    navController: NavController,
    categoryViewModel: CategoryViewModel,
    closeDrawer: () -> Unit
) {
    val allCategories by categoryViewModel.allCategories.collectAsState()
    var expandedCategories by remember { mutableStateOf<Set<Int>>(emptySet()) }

    ModalDrawerSheet {
        Text(
            "Kategorie",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()

        if (allCategories.isEmpty()) {
            Text("Wczytywanie kategorii...", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(allCategories) { category ->
                    val isExpanded = expandedCategories.contains(category.id)

                    NavigationDrawerItem(
                        label = { Text(category.name, style = MaterialTheme.typography.titleSmall) },
                        selected = false,
                        onClick = {
                            expandedCategories = if (isExpanded) {
                                expandedCategories - category.id
                            } else {
                                expandedCategories + category.id
                            }
                        },
                        icon = {
                            if (category.children.isNotEmpty()) {
                                Icon(
                                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Zwiń" else "Rozwiń"
                                )
                            }
                        }
                    )

                    if (isExpanded) {
                        Column(modifier = Modifier.padding(start = 24.dp)) {
                            category.children.forEach { child ->
                                NavigationDrawerItem(
                                    label = { Text(child.name) },
                                    selected = false,
                                    onClick = {
                                        if (child.isLeaf) {
                                            navController.navigate("productlist/category/${child.id}")
                                        } else {
                                            navController.navigate("subcategory/${child.id}")
                                        }
                                        closeDrawer()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
