package org.oakim.pbm.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.File

internal object BaseServiceData {
    private const val credentialsFileName = "cred/credentials-drive.json"
    internal const val baseDirName = "PBM.APP.DB"
    internal const val folderMimeType = "application/vnd.google-apps.folder"
    internal val jsonFactory: JsonFactory by lazy { JacksonFactory.getDefaultInstance() }
    private val scopes = DriveScopes.all() + SheetsScopes.all()
    internal val transport: NetHttpTransport by lazy { GoogleNetHttpTransport.newTrustedTransport() }
    internal val credentials: Credential by lazy {
        val cis = File(credentialsFileName)
                .also { if (!it.exists()) throw Exception("Credential file does not exists") }
                .inputStream()
        val secrets = GoogleClientSecrets
                .load(jsonFactory, cis.reader())
        val flow = GoogleAuthorizationCodeFlow
                .Builder(transport, jsonFactory, secrets, scopes)
                .setDataStoreFactory(FileDataStoreFactory(File("tokens")))
                .setAccessType("offline")
                .build()
        val receiver = LocalServerReceiver
                .Builder()
                .setPort(8888)
                .build()
        AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}