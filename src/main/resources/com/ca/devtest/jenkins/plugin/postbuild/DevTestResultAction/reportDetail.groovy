package com.ca.devtest.jenkins.plugin.postbuild.DevTestResultAction

import com.ca.devtest.jenkins.plugin.Messages


if (my.result.failCount == 0 && my.result.totalCount == 0) {
    h2("${Messages.DevTestReport_NoReports()}")
} else if (my.result.failCount == 0 && my.result.totalCount != 0) {
    h2("${Messages.DevTestReport_AllTestsPassed()}")
    table(id: "fail-tbl", border: "1px", class: "pane sortable") {
        thead() {
            tr() {
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_TestName()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_SuiteName()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_Cycles()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_Duration()}")
                }
            }
        }
        tbody() {
            for (passedTest in my.result.successfullTests) {
                tr() {
                    td(align: "middle") {
                        a(id: "${passedTest.id}-showlink", href: "${passedTest.getTestCaseId()}") {
                            text("${passedTest.name}")
                        }
                        a(style: "display:none", id: "${passedTest.getTestCaseId()}-hidelink", href: "'${passedTest.getTestCaseId()}") {
                            text("<<<")
                        }
                        text(" ")
                        div(id: "${passedTest.id}", style: "display:none", class: "hidden") {
                            text("Loading...")
                        }
                    }
                    td(align: "middle") {
                        text("${passedTest.suiteName}")
                    }
                    td(align: "middle") {
                        text("${passedTest.cycles.size()}")
                    }
                    td(align: "middle") {
                        text("${passedTest.elapsedTimeInMillSec} ms")
                    }
                }
            }
        }
    }
} else {
    h2("${Messages.DevTestReport_FailedTests()}")
    table(id: "fail-tbl", border: "1px", class: "pane sortable") {
        thead() {
            tr() {
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_TestName()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_SuiteName()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_Cycles()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_Duration()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_FailedWithStatus()}")
                }
            }
        }
        tbody() {
            for (failedTest in my.result.failedTests) {
                tr() {
                    td(align: "middle") {
                        a(id: "${failedTest.id}-showlink", href: "${failedTest.getTestCaseId()}") {
                            text("${failedTest.name}")
                        }
                        a(style: "display:none", id: "${failedTest.getTestCaseId()}-hidelink", href: "'${failedTest.getTestCaseId()}") {
                            text("<<<")
                        }
                        text(" ")
                        div(id: "${failedTest.id}", style: "display:none", class: "hidden") {
                            text("Loading...")
                        }
                    }
                    td(align: "middle") {
                        text("${failedTest.suiteName}")
                    }
                    td(align: "middle") {
                        text("${failedTest.cycles.size()}")
                    }
                    td(align: "middle") {
                        text("${failedTest.elapsedTimeInMillSec} ms")
                    }
                    td(align: "middle") {
                        text("${failedTest.state}")
                    }
                }
            }
        }
    }
    h2("${Messages.DevTestReport_PassedTests()}")
    table(id: "fail-tbl", border: "1px", class: "pane sortable") {
        thead() {
            tr() {
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_TestName()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_SuiteName()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_Cycles()}")
                }
                th(class: "pane-header") {
                    text("${Messages.DevTestReport_Duration()}")
                }
            }
        }
        tbody() {
            for (passedTest in my.result.successfullTests) {
                tr() {
                    td(align: "middle") {
                        a(id: "${passedTest.id}-showlink", href: "${passedTest.getTestCaseId()}") {
                            text("${passedTest.name}")
                        }
                        a(style: "display:none", id: "${passedTest.getTestCaseId()}-hidelink", href: "'${passedTest.getTestCaseId()}") {
                            text("<<<")
                        }
                        text(" ")
                        div(id: "${passedTest.id}", style: "display:none", class: "hidden") {
                            text("Loading...")
                        }
                    }
                    td(align: "middle") {
                        text("${passedTest.suiteName}")
                    }
                    td(align: "middle") {
                        text("${passedTest.cycles.size()}")
                    }
                    td(align: "middle") {
                        text("${passedTest.elapsedTimeInMillSec} ms")
                    }
                }
            }
        }
    }

}
