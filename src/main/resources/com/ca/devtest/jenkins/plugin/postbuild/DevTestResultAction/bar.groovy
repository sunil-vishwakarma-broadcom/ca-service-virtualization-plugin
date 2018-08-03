package com.ca.devtest.jenkins.plugin.postbuild.DevTestResultAction

import com.ca.devtest.jenkins.plugin.Messages

div() {
    if (my.result.totalCount == 0) {
        text("${Messages.DevTestReport_NoResults()}")
    } else {
        div(id: "fail-skip") {
            text("${my.result.failCount} ${Messages.DevTestReport_Failures()}")
        }

        div(style: "width:100%; height:1em; background-color: #729FCF") {
            def failpc = my.result.failCount * 100 / my.result.totalCount
            div(style: "width:${failpc}%; height: 1em; background-color: #EF2929; float: left")
        }

        div(id: "pass", align: "right") {
            text("${my.result.totalCount} ${Messages.DevTestReport_Tests()}")
        }
    }
}