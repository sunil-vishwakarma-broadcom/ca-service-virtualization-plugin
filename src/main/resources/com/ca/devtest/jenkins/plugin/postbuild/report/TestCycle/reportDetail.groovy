package com.ca.devtest.jenkins.plugin.postbuild.report.TestCycle

import com.ca.devtest.jenkins.plugin.Messages


div(id: "report") {
    h2("${Messages.DevTestReport_Cycle()} ${my.cycle+1} ${Messages.DevTestReport_Response()}")
    pre(){
        text("${my.rawCycleReport}")
    }

}