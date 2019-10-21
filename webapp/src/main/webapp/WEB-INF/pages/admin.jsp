<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="java.util.Map" %>

<%
    Map<String, String> messages = (Map<String, String>) request.getAttribute("messages");
%>

<div id="webconferencing-admin" class="container-fluid">
    <h3 class="titleWithBorder">${messages["smartactivity.admin.title"]}</h3>
    <div class="content">
        <div id="smartactivity-settings">
            <h4>${messages["smartactivity.admin.setting-title"]}</h4>

            <div id="time-scale" class="VuetifyApp">
                <v-app id="time-scale-app">
                    <v-card flat color="transparent">
                        <v-subheader>${messages["smartactivity.admin.time-scale-setting-title"]}</v-subheader>
                        <v-card-text>
                            <v-slider
                                    v-model="timeScaleModel"
                                    :tick-labels="ticksLabels"
                                    :max="6"
                                    step="1"
                                    ticks="always"
                                    tick-size="7"
                                    v-on:change="selectTimeScale"
                            ></v-slider>
                        </v-card-text>
                    </v-card>
                </v-app>
            </div>

            <div id="stream-selector" class="VuetifyApp">
                <v-app id="stream-selector-app">
                    <v-subheader>Stream selector</v-subheader>
                    <div>
                        <v-select v-on:change="selectStream" v-model="stream" :items="streams"></v-select>
                    </div>
                </v-app>
            </div>

        </div>


        <div class="VuetifyApp">
            <div id="app-smartactivity-table-vue-and-vuetify">
                <v-app id="inspire-test">
                    <v-data-table
                            :headers="headers"
                            :items="tableVal"
                            :items-per-page="10"
                            class="elevation-1"
                            v-on:click:row="selectTableRow"
                            v-on:update:sort-by="cleanTable"
                    ></v-data-table>
                </v-app>
            </div>
        </div>


        <p>context
        <p>${contextJson}</p></p>
        <p>user info
        <p>${remoteUser}</p></p>

        <p>activity focus records</p>
        <p>${activityFocusRecords}</p>



        <p></p>
        <p> /** The user id. */
            @Id
            @Column(name = "USER_ID", nullable = false)
            protected String userId;${activityFocusRecordsObj[0].userId}</p>
        <p>/** The activity id. */
            @Id
            @Column(name = "ACTIVITY_ID", nullable = false)
            protected String activityId;${activityFocusRecordsObj[0].activityId}</p>
        <p>/** The start time. */
            @Id
            @Column(name = "START_TIME", nullable = false)
            protected Long startTime;${activityFocusRecordsObj[0].startTime}</p>
        <p>/** The stop time. */
            @Column(name = "STOP_TIME", nullable = false)
            protected Long stopTime;${activityFocusRecordsObj[0].stopTime}
        </p>
        <p>/** The total show time. */
            @Column(name = "TOTAL_SHOWN", nullable = false)
            protected Long totalShown;${activityFocusRecordsObj[0].totalShown}</p>
        <p> /** The content show time. */
            @Column(name = "CONTENT_SHOWN")
            protected Long contentShown;${activityFocusRecordsObj[0].contentShown}</p>
        <p>/** The conversation show time. */
            @Column(name = "CONVO_SHOWN")
            protected Long convoShown;${activityFocusRecordsObj[0].convoShown}</p>
        <p> /** The content hits. */
            @Column(name = "CONTENT_HITS")
            protected Long contentHits;${activityFocusRecordsObj[0].contentHits}</p>
        <p>/** The convo hits. */
            @Column(name = "CONVO_HITS")
            protected Long convoHits;${activityFocusRecordsObj[0].convoHits}</p>
        <p>/** The app hits. */
            @Column(name = "APP_HITS")
            protected Long appHits;${activityFocusRecordsObj[0].appHits}</p>
        <p> /** The profile hits. */
            @Column(name = "PROFILE_HITS")
            protected Long profileHits;${activityFocusRecordsObj[0].profileHits}</p>
        <p>/** The link hits. */
            @Column(name = "LINK_HITS")
            protected Long linkHits;${activityFocusRecordsObj[0].linkHits}</p>


    </div>
</div>