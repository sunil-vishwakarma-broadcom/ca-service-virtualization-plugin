package com.ca.devtest.jenkins.plugin.postbuild.DevTestResultAction

import com.ca.devtest.jenkins.plugin.Messages

l = namespace(lib.LayoutTagLib)
st = namespace("jelly:stapler")

l.layout(title: "${Messages.DevTestReport_Title()} for Build #${my.run.number}") {
    st.include(page: "sidepanel.jelly", it: my.run)
    l.main_panel() {

        h1("${my.displayName}")
        st.include(page: "bar.groovy")
        st.include(page: "reportDetail.groovy")
    }
}