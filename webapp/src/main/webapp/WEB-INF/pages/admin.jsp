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

                            v-on:item-expanded="selectTableRow"
                            :single-expand="true"
                            :expanded.sync="expanded"
                            class="elevation-1"
                            show-expand
                            item-key="activityTitle"

                    >

                        <template v-slot:top>
                            <v-toolbar flat color="white">
                                <v-toolbar-title>Smartactivity Table</v-toolbar-title>
                                <v-spacer></v-spacer>
                            </v-toolbar>
                        </template>
                        <template v-slot:expanded-item="{ headers, item}">

                            <td id="subtable-column" :colspan="14">
                                <div id="subtable">
                                    <v-app id="subtable-app" >
                                        <v-data-table
                                                :headers="headers"
                                                :items="appSmartactivityTableVueAndVuetifySubtableValues"
                                                :items-per-page="100"
                                                sort-by="startTimeStatistics"
                                                class="elevation-2 grey lighten-4"
                                        ></v-data-table>
                                    </v-app>
                                </div>
                            </td>
                        </template>

                    </v-data-table>
                </v-app>
            </div>
        </div>


    </div>
</div>