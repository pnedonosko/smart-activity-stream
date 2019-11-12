<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

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


    <div id="app-smartactivity-table-vue-and-vuetify-wrapper" class="VuetifyApp">
      <v-container id="app-smartactivity-table-vue-and-vuetify" fluid>
        <v-app id="inspire-test">


          <v-btn depressed large v-on:click="getDataForTheTable">Apply</v-btn>

          <v-data-table
              :headers="headers"
              :items="tableVal"
              :items-per-page="10"
              :search="search"
              v-on:item-expanded="selectTableRow"
              :single-expand="true"
              :expanded.sync="expanded"
              class="elevation-1"
              show-expand
              item-key="activityCreated"
          >

            <template v-slot:item.focus_chart_data="{ item }">
              <v-sheet
                  :width="100"
                  :height="70"
                  :elevation="0"
              >
                <div v-bind:id="drawChart(item)" class='chart' style="width: 100%; height: 100%;"></div>
              </v-sheet>
            </template>

            <template v-slot:item.activity_title="{ item }">
              <p class="activity-data-title" style="margin: 0;">{{ item.activity_title }}</p>
              <v-chip color="rgb(193,204,240)" x-small="true" dark>{{ item.activityStreamPrettyId }}</v-chip>
            </template>

            <template v-slot:top>
              <v-toolbar flat color="white">
                <v-toolbar-title>Smartactivity Table</v-toolbar-title>
                <v-spacer></v-spacer>

              </v-toolbar>

              <v-text-field
                  v-model="search"
                  append-icon="search"
                  label="Filter"
                  single-line
                  class="mx-4"

                  hide-details
              ></v-text-field>
              <v-container>
              </v-container>

            </template>

            <template v-slot:expanded-item="{headers, item}">

              <td id="subtable-column" :colspan="14">
                <div id="subtable">
                  <v-app id="subtable-app">
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
      </v-container>
    </div>
  </div>

  <div id="chart_div" style="width: 100%; height: 100px;"></div>


  <div id="hidden-space-selector-data">
    <div id="user-spaces">
      <ul>

        <c:forEach items="${userSpaces}" var="spaceItem" varStatus="myItemStat">
          <li class="user-space">
              ${spaceItem.displayName}
          </li>
        </c:forEach>

      </ul>
    </div>
    <div id="user-connections">
      <ul>

        <c:forEach items="${userConnections}" var="userFullNameItem" varStatus="myItemStat">
          <li class="user-connection">
              ${userFullNameItem}
          </li>
        </c:forEach>

      </ul>
    </div>
  </div>

</div>
</div>