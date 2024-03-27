package com.taewooyo.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement


class SerializableDefaultValueDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(UClass::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                val ktClass = node.sourcePsi as? KtClass ?: return
                node.getAnnotation(KOTLINX_SERIALIZABLE) ?: return
                val contents = context.getContents() ?: return
                ktClass.primaryConstructorParameters
                    .filterNot { it.hasDefaultValue() }
                    .forEach { ktParameter ->
                        val (startOffset, endOffset) = findErrorLocation(contents, ktParameter)
                        val location = Location.create(
                            context.file,
                            context.getContents(),
                            startOffset,
                            endOffset
                        )
                        context.report(
                            issue = ISSUE,
                            scope = ktParameter,
                            location = location,
                            message = "Assign a default value to that field.",
                            quickfixData = createDefaultValueFix(
                                ktParameter,
                                location,
                                contents.substring(startOffset, endOffset)
                            )
                        )
                    }
            }
        }
    }

    private fun findErrorLocation(
        contents: CharSequence,
        ktParameter: KtParameter
    ): Pair<Int, Int> {
        var startOffset =
            ktParameter.textRange.startOffset + ktParameter.annotationEntries.sumOf { it.textLength }
        while (startOffset < contents.length && contents[startOffset].isWhitespace()) {
            startOffset++
        }
        val endOffset = ktParameter.textRange.endOffset
        return startOffset to endOffset
    }

    private fun createDefaultValueFix(param: KtParameter, location: Location, target: String): LintFix {
        val typeName = param.typeReference?.text
        val defaultValue = getDefaultForType(typeName)
        val fixedSource = "$target = $defaultValue"
        return fix()
            .replace()
            .range(location)
            .text(target)
            .with(fixedSource)
            .build()
    }

    companion object {
        private const val KOTLINX_SERIALIZABLE = "kotlinx.serialization.Serializable"

        val ISSUE: Issue = Issue.create(
            id = "SerializableDefaultValueDetector",
            briefDescription = "Please assign default values in Serializable Annotation's data class.",
            explanation = "If the default value does not exist, a `MissingFieldException` may be thrown.",
            priority = 1,
            severity = Severity.ERROR,
            implementation = Implementation(
                SerializableDefaultValueDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}

internal fun getDefaultForType(typeName: String?): String {
    val cleanedTypeName = typeName?.replace(regex = Regex("<.*?>"), replacement = "")
    return cleanedTypeName?.let {
        when (cleanedTypeName) {
            "Int" -> "0"
            "Long" -> "0L"
            "Double" -> "0.0"
            "Float" -> "0f"
            "Boolean" -> "false"
            "String" -> "\"\""
            "List" -> "emptyList()"
            "MutableList" -> "mutableListOf()"
            "Set" -> "emptySet()"
            "MutableSet" -> "mutableSetOf()"
            "Map" -> "emptyMap()"
            "MutableMap" -> "mutableMapOf()"
            "JsonElement" -> "JsonNull"
            else -> "$cleanedTypeName()"
        }
    } ?: "null"
}
