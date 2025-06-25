package com.example.tarot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.R
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.MysticaBrandStyle
import com.example.tarot.ui.theme.MysticaSubtitleStyle
import com.example.tarot.ui.theme.MysticaTaglineStyle
import com.example.tarot.ui.theme.TextAccent
import com.example.tarot.ui.theme.TextSecondary

@Composable
fun MysticaLogo(
    modifier: Modifier = Modifier,
    logoSize: Int = 120
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Mystical stars decoration above logo
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "✦",
                fontSize = 20.sp,
                color = MysticGold.copy(alpha = 0.8f),
                modifier = Modifier.alpha(0.7f)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "✧",
                fontSize = 16.sp,
                color = MysticSilver.copy(alpha = 0.6f),
                modifier = Modifier.alpha(0.5f)
            )
            Spacer(modifier = Modifier.width(32.dp))
            Text(
                text = "✦",
                fontSize = 18.sp,
                color = MysticGold.copy(alpha = 0.9f),
                modifier = Modifier.alpha(0.8f)
            )
        }

        // Main Logo Image
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // Glow effect behind logo
            Image(
                painter = painterResource(id = R.drawable.mystica_logo),
                contentDescription = "Mystica Logo",
                modifier = Modifier
                    .size(logoSize.dp)
                    .alpha(0.3f)
            )

            // Main logo
            Image(
                painter = painterResource(id = R.drawable.mystica_logo),
                contentDescription = "Mystica Logo",
                modifier = Modifier.size(logoSize.dp)
            )
        }

        // MYSTICA Brand Text
        Text(
            text = "MYSTICA",
            style = MysticaBrandStyle.copy(
                shadow = Shadow(
                    color = MysticGold.copy(alpha = 0.5f),
                    blurRadius = 8f
                )
            ),
            color = TextAccent,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Mystical ornamental divider
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "⟡",
                fontSize = 14.sp,
                color = MysticGold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "◊",
                fontSize = 16.sp,
                color = MysticSilver,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "⟡",
                fontSize = 14.sp,
                color = MysticGold,
                letterSpacing = 2.sp
            )
        }

        // Tagline
        Text(
            text = "Ancient Wisdom • Modern Magic",
            style = MysticaTaglineStyle,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Subtitle
        Text(
            text = "TAROT & DIVINATION",
            style = MysticaSubtitleStyle.copy(
                shadow = Shadow(
                    color = MysticGold.copy(alpha = 0.3f),
                    blurRadius = 4f
                )
            ),
            color = MysticGold.copy(alpha = 0.9f)
        )

        // Mystical stars decoration below
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "✧",
                fontSize = 14.sp,
                color = MysticSilver.copy(alpha = 0.4f),
                modifier = Modifier.alpha(0.6f)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "✦",
                fontSize = 16.sp,
                color = MysticGold.copy(alpha = 0.7f),
                modifier = Modifier.alpha(0.8f)
            )
            Spacer(modifier = Modifier.width(28.dp))
            Text(
                text = "✧",
                fontSize = 12.sp,
                color = MysticSilver.copy(alpha = 0.5f),
                modifier = Modifier.alpha(0.4f)
            )
        }
    }
}

@Composable
fun MysticaLogoCompact(
    modifier: Modifier = Modifier,
    logoSize: Int = 80
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Smaller logo for compact version
        Image(
            painter = painterResource(id = R.drawable.mystica_logo),
            contentDescription = "Mystica Logo",
            modifier = Modifier
                .size(logoSize.dp)
                .padding(bottom = 12.dp)
        )

        // Simplified text
        Text(
            text = "MYSTICA",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 4.sp
            ),
            color = TextAccent,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "TAROT & DIVINATION",
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            ),
            color = MysticGold.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A0D2E)
@Composable
fun MysticaLogoPreview() {
    MysticaLogo()
}

@Preview(showBackground = true, backgroundColor = 0xFF1A0D2E)
@Composable
fun MysticaLogoCompactPreview() {
    MysticaLogoCompact()
}