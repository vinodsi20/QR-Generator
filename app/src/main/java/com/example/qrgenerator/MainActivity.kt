package com.example.qrgenerator

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qrgenerator.ui.theme.QRGeneratorTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRGeneratorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QrGeneratorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun QrGeneratorScreen(modifier: Modifier = Modifier) {
    var inputText by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QR Code Generator",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Mobile, Email, URL, or UPI ID") },
            placeholder = { Text("e.g., name@bank, 9876543210, https://...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (inputText.isNotBlank()) {
                    val textToEncode = if (isUpiId(inputText)) {
                        "upi://pay?pa=$inputText"
                    } else {
                        inputText
                    }
                    qrBitmap = generateQrCode(textToEncode)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate QR Code")
        }

        Spacer(modifier = Modifier.height(32.dp))

        qrBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Generated QR Code",
                modifier = Modifier.size(250.dp)
            )
        } ?: Text("Enter details and click generate")
    }
}

/**
 * Basic heuristic to check if input is a UPI ID (e.g., username@bank)
 */
fun isUpiId(input: String): Boolean {
    return input.contains("@") && 
           !input.startsWith("http", ignoreCase = true) && 
           !input.contains(".com", ignoreCase = true) &&
           !input.contains(".net", ignoreCase = true)
}

fun generateQrCode(text: String): Bitmap {
    val size = 512
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

@Preview(showBackground = true)
@Composable
fun QrGeneratorPreview() {
    QRGeneratorTheme {
        QrGeneratorScreen()
    }
}
