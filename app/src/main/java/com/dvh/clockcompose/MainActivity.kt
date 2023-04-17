package com.dvh.clockcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dvh.clockcompose.ui.theme.ClockComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val modifier = Modifier
            ClockComposeTheme {
                Box(modifier = modifier.fillMaxSize()) {
                    CustomAnalogClock(24f, modifier)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val modifier = Modifier
    ClockComposeTheme {
        Box(modifier = modifier.fillMaxSize()) {
            CustomAnalogClock(24f, modifier)
        }
    }
}