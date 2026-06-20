package com.unitbv.fmi.fitnessapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.unitbv.fmi.fitnessapp.navigation.MainNavigation
import com.unitbv.fmi.fitnessapp.ui.theme.UnitBvFMI2026Theme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		Log.d("MainActivity", "onCreate")
		setContent {
			UnitBvFMI2026Theme {
				MainNavigation()
			}
		}
	}

	override fun onStart() {
		super.onStart()
		Log.d("MainActivity", "onStart")
	}

	override fun onResume() {
		super.onResume()
		Log.d("MainActivity", "onResume")
	}

	override fun onPause() {
		super.onPause()
		Log.d("MainActivity", "onPause")
	}

	override fun onStop() {
		super.onStop()
		Log.d("MainActivity", "onStop")
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.d("MainActivity", "onDestroy")
	}
}