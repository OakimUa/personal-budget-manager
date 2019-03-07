package org.oakim.pbm.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.collections.ObservableList
import org.oakim.pbm.data.Tag
import org.oakim.pbm.data.Wallet
import org.oakim.pbm.parsing.Parser
import tornadofx.*
import java.io.File

object LocalStorage {
    private val baseDir: File by lazy {
        File(System.getProperty("user.home"), ".pbm.db").apply { if (!exists()) mkdirs() }
    }

    private val json: ObjectMapper by lazy {
        jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    private val yaml: ObjectMapper by lazy {
        ObjectMapper(YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    //------------------------------------------------------------------------------------------------------------------
    // parsers
    //------------------------------------------------------------------------------------------------------------------
    private val parserDir: File by lazy {
        File(baseDir, "parser").apply { if (!exists()) mkdirs() }
    }

    val parserNames: ObservableList<String> by lazy {
        parserDir.list().map { it.removeSuffix(".yaml") }.toList().toMutableList().observable()
    }

    fun saveParser(parser: Parser) {
        val name = parser.name
        yaml.writeValue(File(parserDir, "$name.yaml"), parser.toDSO())
        if (name !in parserNames)
            parserNames += name
    }

    fun loadParser(name: String) = yaml.readValue<Parser.DSO>(File(parserDir, "$name.yaml")).toParser()

    //------------------------------------------------------------------------------------------------------------------
    // transactions
    //------------------------------------------------------------------------------------------------------------------
    val transactions: TransactionLocalLazyLoader = TransactionLocalLazyLoader(baseDir, json)

    //------------------------------------------------------------------------------------------------------------------
    // wallets
    //------------------------------------------------------------------------------------------------------------------
    val wallets: ObservableList<Wallet> by lazy {
        File(baseDir, "wallets.json").let { file ->
            if (file.exists()) {
                json.readValue<List<Wallet.DSO>>(file).map(Wallet.DSO::toWallet).toMutableList().observable()
            } else {
                mutableListOf<Wallet>().observable()
            }
        }.also {
            it.onChange { change -> storeWallets(change.list) }
        }
    }

    fun storeWallets() = storeWallets(wallets)

    fun storeWallets(wallets: List<Wallet>) {
        json.writeValue(File(baseDir, "wallets.json"), wallets.map(Wallet::toDSO))
    }

    //------------------------------------------------------------------------------------------------------------------
    // tags
    //------------------------------------------------------------------------------------------------------------------
    val tagAspects
        get() = tags.map { it.aspect }.toSet()

    val tags: ObservableList<Tag> by lazy {
        File(baseDir, "tags.json").let { file ->
            if (file.exists()) {
                json.readValue<List<Tag>>(file).toMutableList().observable()
            } else {
                mutableListOf<Tag>().observable()
            }
        }.also {
            it.onChange { change -> storeTags(change.list) }
        }
    }

    fun storeTags() = storeTags(tags)

    fun storeTags(tags: List<Tag>) {
        json.writeValue(File(baseDir, "tags.json"), tags)
    }

    //------------------------------------------------------------------------------------------------------------------
    // wallet tags
    //------------------------------------------------------------------------------------------------------------------
    val walletTags: ObservableList<Tag> by lazy {
        File(baseDir, "wallet-tags.json").let { file ->
            if (file.exists()) {
                json.readValue<List<Tag>>(file).toMutableList().observable()
            } else {
                mutableListOf<Tag>().observable()
            }
        }.also {
            it.onChange { change -> storeWalletTags(change.list) }
        }
    }

    fun storeWalletTags() = storeWalletTags(walletTags)

    fun storeWalletTags(tags: List<Tag>) {
        json.writeValue(File(baseDir, "wallet-tags.json"), tags)
    }
    //------------------------------------------------------------------------------------------------------------------
}
