package com.ca.devtest.jenkins.plugin.postbuild.report.TestCase

import com.ca.devtest.jenkins.plugin.Messages

div(id: "report") {
    h1("${my.name} ")

    span(class: "${my.getCssClass()}", id: "status") {
        h3("${my.state}")
    }

    if (my.failedCycles.size() != 0) {
        span() {
            h2("${Messages.DevTestReport_FailedCycles()}")
        }
        table(id: "fail-tbl", border: "1px", class: "pane sortable") {
            thead() {
                tr() {
                    th(class: "pane-header") {
                        text("${Messages.DevTestReport_Cycle()}")
                    }
                    th(class: "pane-header") {
                        text("${Messages.DevTestReport_Duration()}")
                    }
                    th(class: "pane-header") {
                    }
                }
            }
            tbody() {
                my.failedCycles.eachWithIndex { item, index ->
                    tr() {
                        td(align: "middle") {
                            text(item.cycle+1)
                        }
                        td(align: "middle") {
                            text("${item.elapsedTimeInMillSec} ms")
                        }
                        td(align: "middle") {
                            a(href:"${item.getTestCycleId()}") {
                                text("view message")
                            }
                        }
                    }
                }
            }
        }
    }
    if (my.succesfullCycles.size() != 0) {
        span() {
            h2("${Messages.DevTestReport_PassedCycles()}")
        }
        table(id: "fail-tbl", border: "1px", class: "pane sortable") {
            thead() {
                tr() {
                    th(class: "pane-header") {
                        text("${Messages.DevTestReport_Cycle()}")
                    }
                    th(class: "pane-header") {
                        text("${Messages.DevTestReport_Duration()}")
                    }
                    th(class: "pane-header") {
                    }
                }
            }
            tbody() {
                my.succesfullCycles.eachWithIndex { item, index ->
                    tr() {
                        td(align: "middle") {
                            text(item.cycle+1)
                        }
                        td(align: "middle") {
                            text("${item.elapsedTimeInMillSec} ms")
                        }
                        td(align: "middle") {
                            a(href:"${item.getTestCycleId()}") {
                                text("view message")
                            }
                        }
                    }
                }
            }
        }
    }
}