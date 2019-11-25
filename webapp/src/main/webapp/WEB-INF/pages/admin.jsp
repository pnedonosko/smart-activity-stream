<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page import="java.util.Map" %>

<%
  Map<String, String> messages = (Map<String, String>) request.getAttribute("messages");
%>

<div id="webconferencing-admin" class="container-fluid">
  <h3 class="titleWithBorder">${messages["smartactivity.admin.title"]}</h3>
  <div class="content">
    <div id="smartactivity-settings" class="VuetifyApp">

      <div id="time-scale">
        <v-app id="time-scale-app">
          <v-card flat color="transparent">
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

      <div id="stream-selector">
        <v-app id="stream-selector-app">
          <div>
            <v-select v-on:change="selectStream" v-model="stream" :items="streams"></v-select>
          </div>
        </v-app>
      </div>


    </div>


    <div id="app-smartactivity-table-vue-and-vuetify-wrapper" class="VuetifyApp">
      <v-container id="app-smartactivity-table-vue-and-vuetify" fluid>
        <v-app id="inspire-test">

          <v-data-table
              :headers="headers"
              :items="tableVal"
              :items-per-page="7"
              :search="search"
              v-on:item-expanded="selectTableRow"
              :single-expand="true"
              :expanded.sync="expanded"
              :custom-sort="customSort"
              class="elevation-1"
              show-expand
              item-key="activityCreated"
          >

            <template v-slot:item.focus_chart_data="{ item }">
              <v-sheet
                  :width="100"
                  :height="60"
                  :elevation="0"
              >
                <div v-bind:id="drawChart(item)" class='chart' style="width: 100%; height: 100%;"></div>
              </v-sheet>
            </template>

            <template v-slot:item.activity_title="{ item }">
              <p class="activity-data-title" style="margin: 0;">
                <a :href="item.activityUrl" target="_blank">{{ item.activity_title }}</a>
              </p>
              <v-chip color="rgb(193,204,240)" x-small="true" dark>{{ item.activityStreamPrettyId }}</v-chip>
            </template>

            <template v-slot:item.activity_created="{ item }">
              <div class="time-table-block">
                {{item.activityCreated}}
              </div>
            </template>

            <template v-slot:item.activity_updated="{ item }">
              <div class="time-table-block">
                {{item.activityUpdated}}
              </div>
            </template>

            <template v-slot:item.local_start_time="{ item }">
              <div class="time-table-block">
                {{item.localStartTime}}
              </div>
            </template>

            <template v-slot:item.local_stop_time="{ item }">
              <div class="time-table-block">
                {{item.localStopTime}}
              </div>
            </template>

            <template v-slot:top>

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

              <td id="subtable-column" style="padding: 0;" :colspan="15">
                <div id="subtable">
                  <v-app id="subtable-app">
                    <v-data-table
                        :headers="headers"
                        :items="subtableVal"
                        :items-per-page="1000"
                        :search="subtableSearch"
                        sort-by="startTimeStatistics"
                        class="elevation-2 grey lighten-4"
                        item-key="localStartTime"
                    >

                      <template v-slot:item.data-table-expand="{ item }">
                        <div class="subtable-data-table-expand subtable-hidden-value"
                             style="width: 24px;"></div>
                      </template>

                      <template v-slot:item.focus_chart_data="{ item }">
                        <div class="subtable-row-chart-column subtable-hidden-value" style="width: 100px;"></div>
                      </template>

                      <template v-slot:item.activity_title="{ item }">
                        <div class="subtable-activity-title subtable-hidden-value">
                          <p class="subtable-activity-data-title" style="margin: 0; height: 5px">
                            {{item.activity_title}}</p>
                          <v-chip color="rgb(193,204,240)" x-small="true" dark>{{item.activityStreamPrettyId}}</v-chip>
                        </div>
                      </template>

                      <template v-slot:item.activity_created="{ item }">
                        <div class="subtable-acitivty-reated time-table-block subtable-hidden-value">
                          {{item.activityCreated}}
                        </div>
                      </template>

                      <template v-slot:item.activity_updated="{ item }">
                        <div class="subtable-activity-updated time-table-block subtable-hidden-value">
                          {{item.activityUpdated}}
                        </div>
                      </template>

                      <template v-slot:item.local_start_time="{ item }">
                        <div class="time-table-block">
                          {{item.localStartTime}}
                        </div>
                      </template>

                      <template v-slot:item.local_stop_time="{ item }">
                        <div class="time-table-block">
                          {{item.localStopTime}}
                        </div>
                      </template>

                    </v-data-table>
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
          <li class="user-space" id-value="${spaceItem.prettyName}">
              ${spaceItem.displayName}
          </li>
        </c:forEach>

      </ul>
    </div>
    <div id="user-connections">
      <ul>

        <c:forEach items="${userConnections}" var="userConnection" varStatus="myItemStat">
          <li class="user-connection" id-value="${userConnection.id}">
              ${userConnection.fullName}
          </li>
        </c:forEach>

      </ul>
    </div>
  </div>

</div>
</div>