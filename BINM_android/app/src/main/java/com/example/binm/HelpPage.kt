package com.example.binm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpPage(navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val faqs = listOf(
        "Jak założyć konto?" to "Aby założyć konto, przejdź do strony rejestracji i wypełnij formularz.",
        "Zapomniałem hasła – co robić?" to "Użyj opcji 'Przypomnij hasło' na stronie logowania, aby zresetować hasło.",
        "Jak dodać przedmiot do ulubionych?" to "Kliknij ikonę serduszka przy przedmiocie, aby dodać go do ulubionych.",
        "Czy mogę anulować zamówienie?" to "Tak, zamówienie można anulować w ciągu 30 minut od jego złożenia.",
        "Jak skontaktować się z obsługą klienta?" to "Użyj formularza kontaktowego lub wysyłaj maila na support@binm.pl."
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Kategorie BINM",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                repeat(10) { i ->
                    NavigationDrawerItem(
                        label = { Text("Kategoria $i") },
                        selected = false,
                        onClick = { /* kliknięcie kategorii */ }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo"
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("login") }) {
                            Image(
                                painter = painterResource(id = R.drawable.user_icon),
                                contentDescription = "Profil"
                            )
                        }
                    }
                )
            },
            bottomBar = { BottomBar(navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    "Najczęściej zadawane pytania",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                faqs.forEach { (question, answer) ->
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { expanded = !expanded },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(question, style = MaterialTheme.typography.titleMedium)
                            if (expanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(answer, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Nie znalazłeś odpowiedzi? Skontaktuj się z nami poprzez formularz kontaktowy.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
