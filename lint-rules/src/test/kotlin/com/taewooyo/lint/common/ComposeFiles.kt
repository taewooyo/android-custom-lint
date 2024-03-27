package com.taewooyo.lint.common

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestFile
import org.intellij.lang.annotations.Language

/**
 * [TestFile] 로 사용할 @Composable 어노테이션 더미
 */
val ComposableAnnotationFile: TestFile = kotlin(
    """
    package androidx.compose.runtime

    @MustBeDocumented
    @Retention(AnnotationRetention.BINARY)
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.TYPE,
        AnnotationTarget.TYPE_PARAMETER,
        AnnotationTarget.PROPERTY_GETTER,
    )
    annotation class Composable
    """.trimIndent()
)

fun composableTestFile(@Language("kotlin") source: String): TestFile = kotlin(
    """
    package com.taewooyo.dummy

    import androidx.compose.runtime.Composable

    ${source.trimIndent()}
    """.trimIndent()
)