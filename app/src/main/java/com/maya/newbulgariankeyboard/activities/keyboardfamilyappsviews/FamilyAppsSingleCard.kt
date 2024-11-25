package com.maya.newbulgariankeyboard.activities.keyboardfamilyappsviews


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maya.newbulgariankeyboard.R


@Composable
fun AppCard(
    imageRes: Int,
    title: String,
    rating: Float,
    showNewIcon: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                    if (showNewIcon) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "New",

                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                RatingBar(rating = rating)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                modifier = Modifier.size(24.dp),
                tint =    MaterialTheme.colorScheme.secondary)

        }
    }
}

@Composable
fun RatingBar(rating: Float, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray, // Gold color for filled stars
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppCard() {
    Column(modifier = Modifier.fillMaxSize()) {
        AppCard(
            imageRes = R.drawable.ic_keyboard, // Substitute with actual image resource
            title = "All Language Keyboard (Ad)",
            rating = 4.5f,
            showNewIcon = true,
            onClick = { /* Handle click */ }
        )
        AppCard(
            imageRes = R.drawable.ic_translator, // Substitute with actual image resource
            title = "All Language Translator (Ad)",
            rating = 4.3f,
            showNewIcon = false,
            onClick = { /* Handle click */ }
        )
        AppCard(
            imageRes = R.drawable.ic_qr_code, // Substitute with actual image resource
            title = "QR Code & Barcode Scanner (Ad)",
            rating = 4.0f,
            showNewIcon = false,
            onClick = { /* Handle click */ }
        )
    }
}
