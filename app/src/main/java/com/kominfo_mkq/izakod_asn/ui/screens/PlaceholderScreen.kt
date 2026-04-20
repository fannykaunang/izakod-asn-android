package com.kominfo_mkq.izakod_asn.ui.screens

/**
 * Placeholder screen untuk screen yang belum diimplementasi
 * Digunakan sementara untuk navigasi yang sudah dibuat
 */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PlaceholderScreen(
//    title: String,
//    message: String,
//    onBack: () -> Unit
//) {
//    Scaffold(
//        topBar = {
//            IZAKODHeaderBar(
//                title = title,
//                onBack = onBack
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.background)
//                .padding(paddingValues)
//                .padding(24.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant
//                )
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(32.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Construction,
//                        contentDescription = null,
//                        modifier = Modifier.size(64.dp),
//                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
//                    )
//
//                    Text(
//                        text = "Dalam Pengembangan",
//                        style = MaterialTheme.typography.headlineSmall.copy(
//                            fontWeight = FontWeight.Bold
//                        ),
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//
//                    Text(
//                        text = message,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Button(
//                        onClick = onBack,
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = null
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text("Kembali")
//                    }
//                }
//            }
//        }
//    }
//}
