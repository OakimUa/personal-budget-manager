package org.oakim.pbm.service

import com.google.api.services.drive.Drive
import org.oakim.pbm.service.BaseServiceData.baseDirName
import org.oakim.pbm.service.BaseServiceData.credentials
import org.oakim.pbm.service.BaseServiceData.folderMimeType
import org.oakim.pbm.service.BaseServiceData.jsonFactory
import org.oakim.pbm.service.BaseServiceData.transport

object DriveService {
    val service: Drive by lazy {
        Drive.Builder(transport, jsonFactory, credentials)
                .setApplicationName("Budget Manager")
                .build()
    }

    val baseDirId: String by lazy {
        checkBaseDir()
                ?: createBaseDir()
    }

    fun findFileIdInBaseDir(fileName: String): String? = service
            .files()
            .list()
            .setPageSize(1)
            .setQ("name = '$fileName' and '$baseDirId' in parents")
            .setFields("files(id)")
            .execute()
            .files
            .firstOrNull()
            ?.id

    private fun checkBaseDir(): String? = service
            .files()
            .list()
            .setPageSize(1)
            .setQ("name = '$baseDirName'")
            .setFields("files(id)")
            .execute()
            .files
            .firstOrNull()
            ?.id

    private fun createBaseDir(): String = service
            .files()
            .create(com.google.api.services.drive.model.File().apply {
                name = baseDirName
                mimeType = folderMimeType
            })
            .setFields("id")
            .execute()
            .id
}