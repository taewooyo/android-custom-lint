package com.taewooyo.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue

class LintRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(SerializableDefaultValueDetector.ISSUE)

}