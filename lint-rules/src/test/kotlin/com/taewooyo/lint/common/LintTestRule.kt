package com.taewooyo.lint.common

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Issue
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

fun LintTestRule(): LintTestRule = LintTestRuleImpl()

interface LintTestRule : TestRule {

    /**
     * 린트 에러 개수 검증
     *
     * @param files 테스트할 [TestFile] 목록
     * @param issues 검증할 [Issue] 목록
     * @param expectedCount 발생해야 하는 린트 에러 개수
     * @param fixedFile QuickFix가 가능하다면, QuickFix가 적용된 후 [TestFile]
     * 만약 QuickFix가 불가능 하다면 null 입력, default 값은 null
     */
    fun assertErrorCount(
        files: List<TestFile>,
        issues: List<Issue>,
        expectedCount: Int,
        fixedFile: TestFile? = null
    )
}

private class LintTestRuleImpl : LintTestRule {
    override fun apply(base: Statement?, description: Description?) = base

    override fun assertErrorCount(
        files: List<TestFile>,
        issues: List<Issue>,
        expectedCount: Int,
        fixedFile: TestFile?
    ) {
        TestLintTask
            .lint()
            .allowMissingSdk()
            .testModes(TestMode.DEFAULT)
            .files(*files.toComposableTestableFiles())
            .issues(*issues.toTypedArray())
            .run()
            .expectErrorCount(expectedCount)
            .runIf(fixedFile != null) {
                checkFix(
                    fix = null,
                    after = fixedFile!!
                )
            }
    }

    private fun List<TestFile>.toComposableTestableFiles() = ArrayList(this).apply {
        add(ComposableAnnotationFile)
    }.toTypedArray()
}