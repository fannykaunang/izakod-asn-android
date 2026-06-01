package com.kominfo_mkq.izakod_asn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kominfo_mkq.izakod_asn.ui.theme.GradientEndLight
import com.kominfo_mkq.izakod_asn.ui.theme.GradientStartLight

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

fun defaultBottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem(
        route = "dashboard",
        label = "Beranda",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    BottomNavItem(
        route = "report_list",
        label = "Laporan",
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description
    ),
    BottomNavItem(
        route = "statistics",
        label = "Statistik",
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment
    ),
    BottomNavItem(
        route = "profile",
        label = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.PersonOutline
    )
)

@Composable
fun IZAKODBottomNavigationBar(
    currentRoute: String?,
    items: List<BottomNavItem> = defaultBottomNavItems(),
    onNavigate: (String) -> Unit,
    onCreateReport: () -> Unit
) {
    val gradientColors = listOf(GradientStartLight, GradientEndLight)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            items.take(2).forEach { item ->
                BottomNavItemView(
                    item = item,
                    selected = currentRoute == item.route,
                    gradientColors = gradientColors,
                    onClick = { onNavigate(item.route) }
                )
            }

            CenterCreateButton(
                onClick = onCreateReport,
                gradientColors = gradientColors
            )

            items.drop(2).forEach { item ->
                BottomNavItemView(
                    item = item,
                    selected = currentRoute == item.route,
                    gradientColors = gradientColors,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun CenterCreateButton(
    onClick: () -> Unit,
    gradientColors: List<Color>
) {
    val plusIconTint = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .offset(y = (-6).dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.linearGradient(gradientColors))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Buat Laporan",
                    tint = plusIconTint
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItemView(
    item: BottomNavItem,
    selected: Boolean,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val iconColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 3.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, bottom = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                if (selected) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            brush = Brush.linearGradient(gradientColors)
                        ),
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = item.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
