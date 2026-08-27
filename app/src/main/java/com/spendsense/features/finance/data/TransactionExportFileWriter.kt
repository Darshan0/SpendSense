package com.spendsense.features.finance.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class TransactionExportFileWriter(
    private val context: Context,
) {
    fun writeCsv(csv: String): Intent {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "spendsense-transactions-analysis.csv")
        file.writeText(csv)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "SpendSense transaction analysis")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
