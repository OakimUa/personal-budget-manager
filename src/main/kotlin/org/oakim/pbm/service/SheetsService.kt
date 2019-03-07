package org.oakim.pbm.service

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import org.oakim.pbm.service.BaseServiceData.credentials
import org.oakim.pbm.service.BaseServiceData.jsonFactory
import org.oakim.pbm.service.BaseServiceData.transport
import java.time.LocalDate
import java.time.Month


object SheetsService {
    val service: Sheets by lazy {
        Sheets.Builder(transport, jsonFactory, credentials)
                .setApplicationName("Budget Manager")
                .build()
    }

    private const val configSpreadsheetName = "config"
    val configSpreadsheet: Spreadsheet by lazy {
        loadSpreadsheet(configSpreadsheetName) ?: createSpreadsheet(configSpreadsheetName, "wallets", "tags")
    }

    val spreadsheet: Spreadsheet by lazy {
        val sheetName = LocalDate.now().year.toString()
        loadSpreadsheet(sheetName) ?: createSpreadsheet(sheetName, "summary", *Month.values().map { it.value.toString() }.toTypedArray())
    }

    private fun loadSpreadsheet(sheetName: String): Spreadsheet? = DriveService
            .findFileIdInBaseDir(sheetName)
            ?.let { id -> service.spreadsheets().get(id).execute() }

    private fun createSpreadsheet(spreadsheetName: String, vararg sheetNames: String) = service
            .spreadsheets()
            .create(Spreadsheet()
                    .setProperties(SpreadsheetProperties()
                            .setTitle(spreadsheetName))
                    .setSheets(sheetNames.map { Sheet().setProperties(SheetProperties().setTitle(it)) }))
            .setFields("spreadsheetId")
            .execute()
            .also { spreadsheet ->
                DriveService.service
                        .files()
                        .update(spreadsheet.spreadsheetId, null)
                        .setAddParents(DriveService.baseDirId)
                        .setRemoveParents(DriveService.service
                                .files()
                                .get(spreadsheet.spreadsheetId)
                                .setFields("parents")
                                .execute()
                                .parents
                                .joinToString(","))
                        .execute()
            }
}