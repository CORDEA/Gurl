package jp.cordea.gurl

import com.google.gson.Gson
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

open class UrlGenerationTask : DefaultTask() {

    lateinit var jsonPath: String

    lateinit var destPackage: String

    lateinit var destPath: String

    var destClassName: String = "Urls"

    @TaskAction
    fun generate() {
        val stream = BufferedReader(InputStreamReader(FileInputStream(jsonPath)))
        val urls = Gson().fromJson<Array<Urls>>(stream, Array<Urls>::class.java)

        val flavors = mutableMapOf<String, MutableList<Pair<String, String>>>()

        urls.forEach {
            val name = it.name
            it.urls.forEach {
                if (flavors[it.flavor]?.add(name to it.url) == null) {
                    flavors[it.flavor] = mutableListOf(name to it.url)
                }
            }
        }

        flavors.forEach {
            val builder = TypeSpec.objectBuilder(destClassName)
            it.value.forEach {
                builder.addProperty(PropertySpec
                        .builder(it.first, String::class, KModifier.PUBLIC, KModifier.CONST)
                        .initializer("\"${it.second}\"")
                        .build())
            }

            File("$destPath/${it.key}/java").apply {
                parentFile.mkdirs()
                FileSpec.builder(destPackage, destClassName)
                        .addType(builder.build())
                        .build()
                        .writeTo(this)
            }
        }
    }
}
