package com.ca.devtest.jenkins.plugin.postbuild.report.TestCycle

l = namespace(lib.LayoutTagLib)
st = namespace("jelly:stapler")

l.layout(title: "${my.name}") {
    st.include(page: "sidepanel.jelly", it: my.run)
    l.main_panel() {
        st.include(page: "reportDetail.groovy")
    }
}