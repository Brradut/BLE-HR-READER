package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.MyApplicationTheme2

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme{
               Surface(color = Color.Black) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().fillMaxHeight().offset(0.dp, 40.dp)) {
                        Button(onClick = {
                            val intent = Intent(applicationContext, HrSettingsActivity::class.java)
                            startActivity(intent)
                        }, modifier = Modifier.padding(0.dp, 5.dp)) {
                            Text(text = "HR Settings")
                        }
                        Button(onClick = { /*TODO*/ }, modifier = Modifier.padding(0.dp, 5.dp)) {
                            Text(text = "Other Settings")
                        }
                        Button(onClick = { /*TODO*/ }, modifier = Modifier.padding(0.dp, 5.dp)) {
                            Text(text = "Enter")
                        }
                    }
                }
            }
        }

    }
}