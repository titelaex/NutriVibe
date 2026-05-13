package com.unitbv.fmi.fitnessapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.unitbv.fmi.fitnessapp.navigation.MainNavigation
import com.unitbv.fmi.fitnessapp.ui.theme.UnitBvFMI2026Theme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			UnitBvFMI2026Theme {
				MainNavigation()
			}
		}

		Log.e("MainActivity", "onCreate")
	}

	override fun onStart() {
		super.onStart()
		Log.e("MainActivity", "onStart")
	}

	override fun onResume() {
		super.onResume()
		Log.e("MainActivity", "onResume")
	}

	override fun onPause() {
		super.onPause()
		Log.e("MainActivity", "onPause")
	}

	override fun onStop() {
		super.onStop()
		Log.e("MainActivity", "onStop")
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.e("MainActivity", "onDestroy")
	}
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
	Text(
		text = "Hello $name!",
		modifier = modifier
	)
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
	UnitBvFMI2026Theme {
		Greeting("Android")
	}
}